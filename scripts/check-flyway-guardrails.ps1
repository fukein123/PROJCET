[CmdletBinding()]
param(
    [string]$ProjectRoot
)

$ErrorActionPreference = "Stop"

if (-not $ProjectRoot) {
    $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function Add-Failure {
    param(
        [System.Collections.Generic.List[string]]$Failures,
        [string]$Message
    )

    $Failures.Add($Message) | Out-Null
}

function Get-Text {
    param([string]$Path)
    return Get-Content -Raw $Path
}

function Assert-PatternPresent {
    param(
        [string]$Content,
        [string]$Pattern,
        [string]$FailureMessage,
        [System.Collections.Generic.List[string]]$Failures
    )

    if ($Content -notmatch $Pattern) {
        Add-Failure -Failures $Failures -Message $FailureMessage
    }
}

function Assert-PatternAbsent {
    param(
        [string]$Content,
        [string]$Pattern,
        [string]$FailureMessage,
        [System.Collections.Generic.List[string]]$Failures
    )

    if ($Content -match $Pattern) {
        Add-Failure -Failures $Failures -Message $FailureMessage
    }
}

$projectRoot = (Resolve-Path $ProjectRoot).Path
$resourcesRoot = Join-Path $projectRoot "backend/src/main/resources"
$migrationRoot = Join-Path $resourcesRoot "db/migration"
$applicationPath = Join-Path $resourcesRoot "application.yml"
$applicationDevPath = Join-Path $resourcesRoot "application-dev.yml"
$applicationProdPath = Join-Path $resourcesRoot "application-prod.yml"
$schemaPath = Join-Path $resourcesRoot "db/schema.sql"
$dataPath = Join-Path $resourcesRoot "db/data.sql"
$v1Path = Join-Path $migrationRoot "V1__baseline_schema.sql"

$requiredPaths = @(
    $applicationPath,
    $applicationDevPath,
    $applicationProdPath,
    $schemaPath,
    $dataPath,
    $migrationRoot,
    $v1Path
)

foreach ($path in $requiredPaths) {
    if (-not (Test-Path $path)) {
        throw "Required path was not found: $path"
    }
}

$failures = New-Object 'System.Collections.Generic.List[string]'
$successChecks = New-Object 'System.Collections.Generic.List[string]'

$applicationText = Get-Text -Path $applicationPath
Assert-PatternPresent -Content $applicationText -Pattern '(?ms)^\s*spring:\s*.*?^\s*sql:\s*\r?\n\s*init:\s*\r?\n\s*mode:\s*never\b' -FailureMessage 'application.yml must keep spring.sql.init.mode=never.' -Failures $failures
Assert-PatternPresent -Content $applicationText -Pattern '(?ms)^\s*flyway:\s*\r?\n\s*enabled:\s*true\b' -FailureMessage 'application.yml must keep spring.flyway.enabled=true.' -Failures $failures
Assert-PatternPresent -Content $applicationText -Pattern '(?ms)^\s*flyway:\s*.*?^\s*baseline-on-migrate:\s*true\b' -FailureMessage 'application.yml must keep spring.flyway.baseline-on-migrate=true.' -Failures $failures
Assert-PatternPresent -Content $applicationText -Pattern '(?ms)^\s*flyway:\s*.*?^\s*baseline-version:\s*1\b' -FailureMessage 'application.yml must keep spring.flyway.baseline-version=1.' -Failures $failures
$successChecks.Add('application.yml keeps Flyway startup as the only runtime initialization entry.') | Out-Null

foreach ($profilePath in @($applicationDevPath, $applicationProdPath)) {
    $profileText = Get-Text -Path $profilePath
    Assert-PatternAbsent -Content $profileText -Pattern '(?i)\bsql\s*:\s*(.|\r|\n)*?\binit\b' -FailureMessage "$([System.IO.Path]::GetFileName($profilePath)) must not override spring.sql.init settings." -Failures $failures
    Assert-PatternAbsent -Content $profileText -Pattern '(?i)\bflyway\s*:' -FailureMessage "$([System.IO.Path]::GetFileName($profilePath)) must not override Flyway strategy." -Failures $failures
}
$successChecks.Add('profile-specific application files do not override runtime initialization strategy.') | Out-Null

$v1Hash = (Get-FileHash $v1Path -Algorithm SHA256).Hash
$expectedV1Hash = 'BDF5E3E7C8789060926068951D1A21C3045F3B01849597EDD68EF7498AC41ABD'
if ($v1Hash -ne $expectedV1Hash) {
    Add-Failure -Failures $failures -Message "V1__baseline_schema.sql hash mismatch. Expected $expectedV1Hash but found $v1Hash."
} else {
    $successChecks.Add('V1__baseline_schema.sql remains frozen at the approved checksum.') | Out-Null
}

$migrationFiles = Get-ChildItem $migrationRoot -File -Filter '*.sql'
$allowedMigrationNamePattern = '^(B\d+__.+|V\d+__.+|R__.+)\.sql$'
foreach ($file in $migrationFiles) {
    if ($file.Name -notmatch $allowedMigrationNamePattern) {
        Add-Failure -Failures $failures -Message "Unexpected migration filename '$($file.Name)'. Only B*.sql, V*.sql, and R*.sql are allowed."
    }
}
$successChecks.Add('migration directory contains only approved B/V/R SQL naming patterns.') | Out-Null

$bootstrapForbiddenPatterns = @(
    '(?i)\bcreate\s+database\b',
    '(?i)\bcreate\s+user\b',
    '(?i)\bgrant\b',
    '(?i)\bflush\s+privileges\b'
)
$versionedFiles = $migrationFiles | Where-Object { $_.Name -match '^(B\d+__.+|V\d+__.+)\.sql$' }
foreach ($file in $versionedFiles) {
    $text = Get-Text -Path $file.FullName
    foreach ($pattern in $bootstrapForbiddenPatterns) {
        if ($text -match $pattern) {
            Add-Failure -Failures $failures -Message "$($file.Name) must not contain DBA/bootstrap statements such as CREATE DATABASE/USER or GRANT."
            break
        }
    }
}
$successChecks.Add('baseline/versioned migrations stay inside schema-alignment responsibility.') | Out-Null

$repeatableForbiddenPatterns = @(
    '(?i)\bcreate\s+table\b',
    '(?i)\balter\s+table\b',
    '(?i)\bdrop\s+table\b',
    '(?i)\bcreate\s+database\b',
    '(?i)\bcreate\s+user\b',
    '(?i)\bgrant\b',
    '(?i)\bflush\s+privileges\b'
)
$repeatableFiles = $migrationFiles | Where-Object { $_.Name -match '^R__.+\.sql$' }
foreach ($file in $repeatableFiles) {
    $text = Get-Text -Path $file.FullName
    foreach ($pattern in $repeatableForbiddenPatterns) {
        if ($text -match $pattern) {
            Add-Failure -Failures $failures -Message "$($file.Name) must stay in repeatable reference-data scope and cannot contain DDL or DBA/bootstrap statements."
            break
        }
    }
}
$successChecks.Add('repeatable migrations remain limited to rerunnable reference-data changes.') | Out-Null

foreach ($placeholderPath in @($schemaPath, $dataPath)) {
    $placeholderText = Get-Text -Path $placeholderPath
    $placeholderName = [System.IO.Path]::GetFileName($placeholderPath)
    Assert-PatternPresent -Content $placeholderText -Pattern '(?i)Deprecated:' -FailureMessage "$placeholderName must remain a deprecated placeholder note." -Failures $failures
    Assert-PatternAbsent -Content $placeholderText -Pattern '(?i)\b(create|alter|insert|update|delete|grant|drop|truncate)\b' -FailureMessage "$placeholderName must not revert to executable runtime SQL." -Failures $failures
}
$successChecks.Add('schema.sql and data.sql remain retired placeholders instead of runtime SQL entry points.') | Out-Null

if ($failures.Count -gt 0) {
    Write-Output 'Flyway guardrail check failed.'
    Write-Output '-----------------------------'
    $failures | ForEach-Object { Write-Output "- $_" }
    exit 1
}

Write-Output 'Flyway guardrail check passed.'
Write-Output '-----------------------------'
$successChecks | ForEach-Object { Write-Output "- $_" }
