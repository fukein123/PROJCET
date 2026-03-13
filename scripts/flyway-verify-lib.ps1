Set-StrictMode -Version Latest

function Write-Step {
    param([string]$Message)
    Write-Output "[flyway-verify] $Message"
}

function Resolve-RequiredCommand {
    param([string]$Name)

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $command) {
        throw "Required command '$Name' was not found in PATH."
    }
    return $command.Source
}

function New-FlywayVerifyContext {
    param([string]$JavaCommand = "java")

    $repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
    return [pscustomobject]@{
        RepoRoot     = $repoRoot
        BackendRoot  = Join-Path $repoRoot "backend"
        MysqlExe     = Resolve-RequiredCommand "mysql"
        MysqldumpExe = Resolve-RequiredCommand "mysqldump"
        MvnExe       = Resolve-RequiredCommand "mvn"
        JavaCommand  = Resolve-RequiredCommand $JavaCommand
    }
}

function New-MySqlConfig {
    param(
        [string]$ServerHost,
        [int]$Port,
        [string]$User,
        [string]$Password
    )

    return [pscustomobject]@{
        Host     = $ServerHost
        Port     = $Port
        User     = $User
        Password = $Password
    }
}

function Invoke-CheckedProcess {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$DisplayName,
        [string]$WorkingDirectory
    )

    Write-Step $DisplayName
    $stdoutPath = [System.IO.Path]::GetTempFileName()
    $stderrPath = [System.IO.Path]::GetTempFileName()
    try {
        $process = Start-Process -FilePath $FilePath `
            -ArgumentList $Arguments `
            -WorkingDirectory $WorkingDirectory `
            -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath `
            -Wait `
            -PassThru

        $output = @()
        if (Test-Path $stdoutPath) {
            $output += Get-Content $stdoutPath
        }
        if (Test-Path $stderrPath) {
            $output += Get-Content $stderrPath
        }
        $output = $output | Where-Object { $_ -notmatch "Using a password on the command line interface can be insecure" }
    } finally {
        Remove-Item $stdoutPath, $stderrPath -ErrorAction SilentlyContinue
    }

    if ($process.ExitCode -ne 0) {
        if ($output) {
            $output | ForEach-Object { Write-Output $_ }
        }
        throw "$DisplayName failed with exit code $($process.ExitCode)."
    }

    if ($output) {
        $output | ForEach-Object { Write-Output $_ }
    }
}

function Resolve-BackendJar {
    param(
        [pscustomobject]$Context,
        [string]$JarPath
    )

    if ($JarPath) {
        if (-not (Test-Path $JarPath)) {
            throw "Jar file was not found: $JarPath"
        }
        return (Resolve-Path $JarPath).Path
    }

    $jarCandidate = Get-ChildItem (Join-Path $Context.BackendRoot "target") -Filter "backend-*.jar" -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike "*.original" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($jarCandidate) {
        return $jarCandidate.FullName
    }

    $fallback = Join-Path $Context.BackendRoot "target\backend-2.0.0.jar"
    if (-not (Test-Path $fallback)) {
        throw "Jar file was not found: $fallback"
    }
    return $fallback
}

function New-VerificationLogDirectory {
    param(
        [pscustomobject]$Context,
        [string]$Prefix,
        [string]$LogDirectory
    )

    if ($LogDirectory) {
        New-Item -ItemType Directory -Force $LogDirectory | Out-Null
        return (Resolve-Path $LogDirectory).Path
    }

    $path = Join-Path $Context.RepoRoot ("tmp\{0}-{1}" -f $Prefix, (Get-Date -Format "yyyyMMdd-HHmmss"))
    New-Item -ItemType Directory -Force $path | Out-Null
    return $path
}

function Get-JdbcUrl {
    param(
        [pscustomobject]$MySqlConfig,
        [string]$DatabaseName
    )

    return "jdbc:mysql://$($MySqlConfig.Host):$($MySqlConfig.Port)/$($DatabaseName)?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
}

function Invoke-MySqlCapture {
    param(
        [pscustomobject]$Context,
        [pscustomobject]$MySqlConfig,
        [string]$Sql,
        [string]$DatabaseName,
        [switch]$SkipColumnNames
    )

    $sqlEscaped = $Sql.Replace('"', '\"')
    $commandLine = "`"$($Context.MysqlExe)`" --host=$($MySqlConfig.Host) --port=$($MySqlConfig.Port) --user=$($MySqlConfig.User) --password=$($MySqlConfig.Password)"
    if ($DatabaseName) {
        $commandLine += " --database=$DatabaseName"
    }
    if ($SkipColumnNames) {
        $commandLine += " --skip-column-names"
    }
    $commandLine += " --execute=`"$sqlEscaped`""

    $stdoutPath = [System.IO.Path]::GetTempFileName()
    $stderrPath = [System.IO.Path]::GetTempFileName()
    try {
        $process = Start-Process -FilePath "cmd.exe" `
            -ArgumentList @("/c", "`"$commandLine`"") `
            -WorkingDirectory $Context.RepoRoot `
            -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath `
            -Wait `
            -PassThru

        $stdout = @()
        $stderr = @()
        if (Test-Path $stdoutPath) {
            $stdout = Get-Content $stdoutPath
        }
        if (Test-Path $stderrPath) {
            $stderr = Get-Content $stderrPath
        }
        $stdout = $stdout | Where-Object { $_ -notmatch "Using a password on the command line interface can be insecure" }
        $stderr = $stderr | Where-Object { $_ -notmatch "Using a password on the command line interface can be insecure" }

        if ($process.ExitCode -ne 0) {
            ($stdout + $stderr) | ForEach-Object { Write-Output $_ }
            throw "mysql query failed with exit code $($process.ExitCode)."
        }

        return ($stdout + $stderr)
    } finally {
        Remove-Item $stdoutPath, $stderrPath -ErrorAction SilentlyContinue
    }
}

function Reset-Database {
    param(
        [pscustomobject]$Context,
        [pscustomobject]$MySqlConfig,
        [string]$DatabaseName
    )

    Write-Step "Recreating database '$DatabaseName'"
    $sql = "DROP DATABASE IF EXISTS $DatabaseName; CREATE DATABASE $DatabaseName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    Invoke-MySqlCapture -Context $Context -MySqlConfig $MySqlConfig -Sql $sql | ForEach-Object { Write-Output $_ }
}

function Copy-Database {
    param(
        [pscustomobject]$Context,
        [pscustomobject]$MySqlConfig,
        [string]$SourceDatabase,
        [string]$TargetDatabase
    )

    Write-Step "Cloning source database '$SourceDatabase' into '$TargetDatabase'"
    $dumpCommand = "`"$($Context.MysqldumpExe)`" -h $($MySqlConfig.Host) -P $($MySqlConfig.Port) -u $($MySqlConfig.User) -p$($MySqlConfig.Password) --single-transaction --routines --events $SourceDatabase | `"$($Context.MysqlExe)`" -h $($MySqlConfig.Host) -P $($MySqlConfig.Port) -u $($MySqlConfig.User) -p$($MySqlConfig.Password) $TargetDatabase"
    cmd /c $dumpCommand
    if ($LASTEXITCODE -ne 0) {
        throw "Database clone import failed with exit code $LASTEXITCODE."
    }
}

function Start-AppAndWait {
    param(
        [pscustomobject]$Context,
        [pscustomobject]$MySqlConfig,
        [string]$DatabaseName,
        [string]$JarFile,
        [string]$StdoutPath,
        [string]$StderrPath,
        [int]$StartupTimeoutSeconds,
        [hashtable]$AdditionalProperties
    )

    $overridePath = [System.IO.Path]::ChangeExtension($StdoutPath, ".properties")
    try {
        $properties = [ordered]@{
            "server.port"                = "0"
            "spring.datasource.url"      = Get-JdbcUrl -MySqlConfig $MySqlConfig -DatabaseName $DatabaseName
            "spring.datasource.username" = $MySqlConfig.User
            "spring.datasource.password" = $MySqlConfig.Password
        }

        if ($AdditionalProperties) {
            foreach ($entry in $AdditionalProperties.GetEnumerator()) {
                $properties[$entry.Key] = [string]$entry.Value
            }
        }

        $lines = foreach ($entry in $properties.GetEnumerator()) {
            "$($entry.Key)=$($entry.Value)"
        }
        $lines | Set-Content -Path $overridePath -Encoding UTF8

        $arguments = @(
            "-jar",
            $JarFile,
            "--spring.config.additional-location=file:/$($overridePath -replace '\\','/')"
        )

        Remove-Item $StdoutPath, $StderrPath -ErrorAction SilentlyContinue
        $process = Start-Process -FilePath $Context.JavaCommand `
            -ArgumentList $arguments `
            -RedirectStandardOutput $StdoutPath `
            -RedirectStandardError $StderrPath `
            -PassThru

        $started = $false
        for ($i = 0; $i -lt $StartupTimeoutSeconds; $i++) {
            Start-Sleep -Seconds 1

            if (Test-Path $StdoutPath) {
                $content = Get-Content -Raw $StdoutPath
                if ($content -match "Started Application") {
                    $started = $true
                    break
                }
                if ($content -match "APPLICATION FAILED TO START") {
                    break
                }
            }

            if ($process.HasExited) {
                break
            }
        }

        if (-not $process.HasExited) {
            Stop-Process -Id $process.Id -Force
            $process.WaitForExit()
        }

        if (-not $started) {
            Write-Output "--- STDOUT TAIL ---"
            if (Test-Path $StdoutPath) {
                Get-Content $StdoutPath -Tail 80
            }
            Write-Output "--- STDERR TAIL ---"
            if (Test-Path $StderrPath) {
                Get-Content $StderrPath -Tail 40
            }
            throw "Application startup validation failed for database '$DatabaseName'."
        }
    } finally {
        Remove-Item $overridePath -ErrorAction SilentlyContinue
    }
}

function Get-LogMatchCount {
    param(
        [string]$Path,
        [string]$Pattern
    )

    if (-not (Test-Path $Path)) {
        return 0
    }

    return (Select-String -Path $Path -Pattern $Pattern | Measure-Object).Count
}

function Get-HistoryRows {
    param(
        [pscustomobject]$Context,
        [pscustomobject]$MySqlConfig,
        [string]$DatabaseName
    )

    $query = "SELECT installed_rank, COALESCE(version,'NULL'), description, type, success FROM flyway_schema_history ORDER BY installed_rank;"
    return Invoke-MySqlCapture -Context $Context -MySqlConfig $MySqlConfig -Sql $query -DatabaseName $DatabaseName -SkipColumnNames
}

function Get-ScenarioSummary {
    param(
        [string]$ScenarioName,
        [string]$DatabaseName,
        [string]$StdoutPath,
        [string]$StderrPath,
        [string[]]$HistoryRows
    )

    return [pscustomobject]@{
        Scenario       = $ScenarioName
        Database       = $DatabaseName
        StartedCount   = Get-LogMatchCount -Path $StdoutPath -Pattern "Started Application"
        DeprecatedWarn = Get-LogMatchCount -Path $StdoutPath -Pattern "deprecated"
        ExistsWarn     = Get-LogMatchCount -Path $StdoutPath -Pattern "already exists"
        StdoutLog      = $StdoutPath
        StderrLog      = $StderrPath
        HistoryRows    = @($HistoryRows)
    }
}

function Assert-ScenarioHealthy {
    param(
        [pscustomobject]$Summary,
        [string[]]$RequiredHistoryPatterns,
        [string[]]$ForbiddenHistoryPatterns
    )

    if ($Summary.StartedCount -lt 1) {
        throw "Scenario '$($Summary.Scenario)' did not reach the application startup marker."
    }
    if ($Summary.DeprecatedWarn -ne 0) {
        throw "Scenario '$($Summary.Scenario)' still contains deprecated warnings."
    }
    if ($Summary.ExistsWarn -ne 0) {
        throw "Scenario '$($Summary.Scenario)' still contains already-exists warnings."
    }

    $historyText = ($Summary.HistoryRows -join "`n")
    foreach ($pattern in $RequiredHistoryPatterns) {
        if ($historyText -notmatch [regex]::Escape($pattern)) {
            throw "Scenario '$($Summary.Scenario)' is missing expected history pattern '$pattern'."
        }
    }
    foreach ($pattern in $ForbiddenHistoryPatterns) {
        if ($historyText -match [regex]::Escape($pattern)) {
            throw "Scenario '$($Summary.Scenario)' contains forbidden history pattern '$pattern'."
        }
    }
}

function Write-ScenarioSummary {
    param([pscustomobject]$Summary)

    Write-Output ""
    Write-Output "Scenario Summary: $($Summary.Scenario)"
    Write-Output "---------------------------------------"
    Write-Output "Database       : $($Summary.Database)"
    Write-Output "Started count  : $($Summary.StartedCount)"
    Write-Output "Deprecated warn: $($Summary.DeprecatedWarn)"
    Write-Output "Exists warn    : $($Summary.ExistsWarn)"
    Write-Output "Stdout log     : $($Summary.StdoutLog)"
    Write-Output "Stderr log     : $($Summary.StderrLog)"
    Write-Output "flyway_schema_history"
    Write-Output "---------------------"
    $Summary.HistoryRows | ForEach-Object { Write-Output $_ }
}

function Write-MatrixSummary {
    param(
        [pscustomobject[]]$Summaries,
        [string]$SummaryPath
    )

    $payload = [pscustomobject]@{
        generatedAt = (Get-Date).ToString("s")
        scenarios   = $Summaries
    }
    $payload | ConvertTo-Json -Depth 6 | Set-Content -Path $SummaryPath -Encoding UTF8
}


