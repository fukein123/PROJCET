[CmdletBinding()]
param(
    [string]$SourceDatabase = "cvs",
    [string]$CloneDatabase = "cvs_flyway_verify",
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
$logDirectory = New-VerificationLogDirectory -Context $context -Prefix "flyway-verify" -LogDirectory $LogDirectory

if (-not $SkipPackage) {
    Invoke-CheckedProcess -FilePath $context.MvnExe -Arguments @("-DskipTests", "package") -DisplayName "Packaging backend jar" -WorkingDirectory $context.BackendRoot
    $jarPath = Resolve-BackendJar -Context $context -JarPath $JarPath
}

Reset-Database -Context $context -MySqlConfig $mySqlConfig -DatabaseName $CloneDatabase
Copy-Database -Context $context -MySqlConfig $mySqlConfig -SourceDatabase $SourceDatabase -TargetDatabase $CloneDatabase

$stdoutPath = Join-Path $logDirectory "startup.out.log"
$stderrPath = Join-Path $logDirectory "startup.err.log"
Write-Step "Starting application against clone database '$CloneDatabase'"
Start-AppAndWait `
    -Context $context `
    -MySqlConfig $mySqlConfig `
    -DatabaseName $CloneDatabase `
    -JarFile $jarPath `
    -StdoutPath $stdoutPath `
    -StderrPath $stderrPath `
    -StartupTimeoutSeconds $StartupTimeoutSeconds `
    -AdditionalProperties @{}

$summary = Get-ScenarioSummary `
    -ScenarioName "clone-verification" `
    -DatabaseName $CloneDatabase `
    -StdoutPath $stdoutPath `
    -StderrPath $stderrPath `
    -HistoryRows (Get-HistoryRows -Context $context -MySqlConfig $mySqlConfig -DatabaseName $CloneDatabase)

Write-Output ""
Write-Output "Flyway Clone Verification Summary"
Write-Output "--------------------------------"
Write-Output "Source database : $SourceDatabase"
Write-Output "Clone database  : $CloneDatabase"
Write-Output "Jar path        : $jarPath"
Write-Output "Logs            : $logDirectory"
Write-Output "Started count   : $($summary.StartedCount)"
Write-Output "Deprecated warn : $($summary.DeprecatedWarn)"
Write-Output "Exists warn     : $($summary.ExistsWarn)"
Write-Output ""
Write-Output "flyway_schema_history"
Write-Output "---------------------"
$summary.HistoryRows | ForEach-Object { Write-Output $_ }

if ($summary.StartedCount -lt 1) {
    throw "Application startup marker was not found in stdout log."
}

Write-Step "Verification completed successfully"

