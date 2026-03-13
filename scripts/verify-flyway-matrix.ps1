[CmdletBinding()]
param(
    [string]$SourceDatabase = "cvs",
    [string]$DatabasePrefix = "cvs_flyway_matrix",
    [string]$MySqlHost = "localhost",
    [int]$MySqlPort = 3306,
    [string]$MySqlUser = "root",
    [string]$MySqlPassword = "123456",
    [string]$JarPath,
    [string]$JavaCommand = "java",
    [int]$StartupTimeoutSeconds = 90,
    [string]$LogDirectory,
    [switch]$SkipPackage
)

$ErrorActionPreference = "Stop"
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

. (Join-Path $PSScriptRoot "flyway-verify-lib.ps1")

$context = New-FlywayVerifyContext -JavaCommand $JavaCommand
$mySqlConfig = New-MySqlConfig -ServerHost $MySqlHost -Port $MySqlPort -User $MySqlUser -Password $MySqlPassword
$jarPath = Resolve-BackendJar -Context $context -JarPath $JarPath
$logDirectory = New-VerificationLogDirectory -Context $context -Prefix "flyway-matrix" -LogDirectory $LogDirectory

if (-not $SkipPackage) {
    Invoke-CheckedProcess -FilePath $context.MvnExe -Arguments @("-DskipTests", "package") -DisplayName "Packaging backend jar" -WorkingDirectory $context.BackendRoot
    $jarPath = Resolve-BackendJar -Context $context -JarPath $JarPath
}

$summaries = New-Object System.Collections.Generic.List[object]

function Invoke-Scenario {
    param(
        [string]$ScenarioName,
        [string]$DatabaseName,
        [scriptblock]$SetupAction,
        [hashtable]$AdditionalProperties,
        [string[]]$RequiredHistoryPatterns,
        [string[]]$ForbiddenHistoryPatterns
    )

    $scenarioDirectory = Join-Path $logDirectory $ScenarioName
    New-Item -ItemType Directory -Force $scenarioDirectory | Out-Null

    & $SetupAction

    $stdoutPath = Join-Path $scenarioDirectory "startup.out.log"
    $stderrPath = Join-Path $scenarioDirectory "startup.err.log"

    Write-Step "Starting scenario '$ScenarioName' against '$DatabaseName'"
    Start-AppAndWait `
        -Context $context `
        -MySqlConfig $mySqlConfig `
        -DatabaseName $DatabaseName `
        -JarFile $jarPath `
        -StdoutPath $stdoutPath `
        -StderrPath $stderrPath `
        -StartupTimeoutSeconds $StartupTimeoutSeconds `
        -AdditionalProperties $AdditionalProperties

    $historyRows = Get-HistoryRows -Context $context -MySqlConfig $mySqlConfig -DatabaseName $DatabaseName
    $summary = Get-ScenarioSummary -ScenarioName $ScenarioName -DatabaseName $DatabaseName -StdoutPath $stdoutPath -StderrPath $stderrPath -HistoryRows $historyRows
    Assert-ScenarioHealthy -Summary $summary -RequiredHistoryPatterns $RequiredHistoryPatterns -ForbiddenHistoryPatterns $ForbiddenHistoryPatterns
    Write-ScenarioSummary -Summary $summary
    $summaries.Add($summary) | Out-Null
}

$emptyDatabase = "${DatabasePrefix}_empty"
Invoke-Scenario `
    -ScenarioName "empty-new-database" `
    -DatabaseName $emptyDatabase `
    -SetupAction {
        Reset-Database -Context $context -MySqlConfig $mySqlConfig -DatabaseName $emptyDatabase
    } `
    -AdditionalProperties @{} `
    -RequiredHistoryPatterns @("baseline schema", "legacy schema alignment", "seed reference data") `
    -ForbiddenHistoryPatterns @("Flyway Baseline")

$cloneDatabase = "${DatabasePrefix}_clone"
Invoke-Scenario `
    -ScenarioName "non-empty-first-adoption" `
    -DatabaseName $cloneDatabase `
    -SetupAction {
        Reset-Database -Context $context -MySqlConfig $mySqlConfig -DatabaseName $cloneDatabase
        Copy-Database -Context $context -MySqlConfig $mySqlConfig -SourceDatabase $SourceDatabase -TargetDatabase $cloneDatabase
    } `
    -AdditionalProperties @{} `
    -RequiredHistoryPatterns @("Flyway Baseline", "legacy schema alignment", "seed reference data") `
    -ForbiddenHistoryPatterns @("baseline schema")

$historyDatabase = "${DatabasePrefix}_history"
$historyScenarioDirectory = Join-Path $logDirectory "existing-v1-history"
$legacyMigrationDirectory = Join-Path $historyScenarioDirectory "legacy-migrations"
New-Item -ItemType Directory -Force $historyScenarioDirectory, $legacyMigrationDirectory | Out-Null
Copy-Item (Join-Path $context.BackendRoot "src/main/resources/db/migration/V1__baseline_schema.sql") $legacyMigrationDirectory -Force
Copy-Item (Join-Path $context.BackendRoot "src/main/resources/db/migration/R__seed_reference_data.sql") $legacyMigrationDirectory -Force

Reset-Database -Context $context -MySqlConfig $mySqlConfig -DatabaseName $historyDatabase
Write-Step "Bootstrapping legacy version-1 history in '$historyDatabase'"
$legacyStdoutPath = Join-Path $historyScenarioDirectory "bootstrap.out.log"
$legacyStderrPath = Join-Path $historyScenarioDirectory "bootstrap.err.log"
$legacyLocations = "filesystem:$($legacyMigrationDirectory -replace '\\','/')"
Start-AppAndWait `
    -Context $context `
    -MySqlConfig $mySqlConfig `
    -DatabaseName $historyDatabase `
    -JarFile $jarPath `
    -StdoutPath $legacyStdoutPath `
    -StderrPath $legacyStderrPath `
    -StartupTimeoutSeconds $StartupTimeoutSeconds `
    -AdditionalProperties @{ "spring.flyway.locations" = $legacyLocations }

Invoke-Scenario `
    -ScenarioName "existing-v1-history" `
    -DatabaseName $historyDatabase `
    -SetupAction { } `
    -AdditionalProperties @{} `
    -RequiredHistoryPatterns @("baseline schema", "legacy schema alignment", "seed reference data") `
    -ForbiddenHistoryPatterns @("Flyway Baseline")

$summaryPath = Join-Path $logDirectory "matrix-summary.json"
Write-MatrixSummary -Summaries $summaries.ToArray() -SummaryPath $summaryPath

Write-Output ""
Write-Output "Flyway Verification Matrix Summary"
Write-Output "---------------------------------"
Write-Output "Source database : $SourceDatabase"
Write-Output "Database prefix : $DatabasePrefix"
Write-Output "Jar path        : $jarPath"
Write-Output "Logs            : $logDirectory"
Write-Output "Summary json    : $summaryPath"

Write-Step "Matrix verification completed successfully"

