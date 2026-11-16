<#
.SYNOPSIS
  Interactive runner: pick a tenant, verify the connection, choose Sanity or Regression, run, and print results.

.DESCRIPTION
  Does NOT depend on machine/user environment variables or on the cx CLI config file for tenant details.
  Everything is applied to this run's process environment only (the tests and the cx CLI inherit it).
  Non-secret tenant details can be saved to tenants\<name>.json (secrets are never written to disk).

.EXAMPLE
  .\run-tests.ps1
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
$tenantDir = Join-Path $root 'tenants'

function Read-Value([string]$Prompt, [string]$Default) {
    $suffix = if ($Default) { " [$Default]" } else { '' }
    $v = Read-Host "$Prompt$suffix"
    if ([string]::IsNullOrWhiteSpace($v)) { return $Default }
    return $v.Trim()
}

function Read-Secret([string]$Prompt) {
    $ss = Read-Host $Prompt -AsSecureString
    return [System.Net.NetworkCredential]::new('', $ss).Password
}

function Select-Option([string]$Title, [string[]]$Options) {
    Write-Host ""
    Write-Host $Title -ForegroundColor Cyan
    for ($i = 0; $i -lt $Options.Count; $i++) { Write-Host ("  {0}) {1}" -f ($i + 1), $Options[$i]) }
    while ($true) {
        $a = Read-Host 'Enter number'
        $n = 0
        if ([int]::TryParse($a, [ref]$n) -and $n -ge 1 -and $n -le $Options.Count) { return $n - 1 }
        Write-Host 'Invalid choice.' -ForegroundColor Yellow
    }
}

# ---------------------------------------------------------------- tenant details
function Get-TenantDetails {
    $saved = @()
    if (Test-Path $tenantDir) { $saved = @(Get-ChildItem $tenantDir -Filter *.json | ForEach-Object { $_.BaseName }) }

    $profile = $null
    if ($saved.Count -gt 0) {
        $idx = Select-Option 'Select a saved tenant or enter a new one:' ($saved + 'New tenant')
        if ($idx -lt $saved.Count) {
            $profile = Get-Content (Join-Path $tenantDir "$($saved[$idx]).json") -Raw | ConvertFrom-Json
        }
    }

    $d = [ordered]@{}
    if ($profile) {
        # Saved tenant: reuse the stored non-secret details, ask only for the secrets (never stored).
        Write-Host "`nUsing saved tenant '$($profile.name)':" -ForegroundColor Cyan
        Write-Host "  CX_BASE_URI      : $($profile.baseUri)"
        Write-Host "  CX_BASE_AUTH_URI : $($profile.baseAuthUri)"
        Write-Host "  CX_TENANT        : $($profile.tenant)"
        Write-Host "  CX_CLIENT_ID     : $($profile.clientId)"
        $d.fromSaved   = $true
        $d.name        = $profile.name
        $d.baseUri     = $profile.baseUri
        $d.baseAuthUri = $profile.baseAuthUri
        $d.tenant      = $profile.tenant
        $d.clientId    = $profile.clientId
        $d.apiKey       = Read-Secret 'CX_APIKEY        (hidden)'
        $d.clientSecret = Read-Secret 'CX_CLIENT_SECRET (hidden)'
    }
    else {
        Write-Host "`nEnter tenant details." -ForegroundColor Cyan
        $d.fromSaved   = $false
        $d.name        = ''
        $d.baseUri     = Read-Value 'CX_BASE_URI      (e.g. https://eu.iam.checkmarx.net/)' ''
        $d.baseAuthUri = Read-Value 'CX_BASE_AUTH_URI (e.g. https://eu.ast.checkmarx.net/)' ''
        $d.tenant      = Read-Value 'CX_TENANT        (tenant name)' ''
        $d.apiKey      = Read-Secret 'CX_APIKEY        (hidden)'
        $d.clientId    = Read-Value 'CX_CLIENT_ID' ''
        $d.clientSecret = Read-Secret 'CX_CLIENT_SECRET (hidden)'
    }

    foreach ($k in 'baseUri', 'baseAuthUri', 'tenant', 'apiKey', 'clientId', 'clientSecret') {
        if ([string]::IsNullOrWhiteSpace($d[$k])) { throw "Value '$k' is required." }
    }
    return $d
}

# ---------------------------------------------------------------- cx CLI
function Get-CxPath {
    $p = $env:CX_CLI_PATH
    if ($p) {
        if ($p -notmatch 'cx\.exe$') { $p = Join-Path $p 'cx.exe' }
        if (Test-Path $p) { return $p }
    }
    $cmd = Get-Command cx -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $entered = Read-Host 'Path to cx.exe (or its folder)'
    if ($entered -notmatch 'cx\.exe$') { $entered = Join-Path $entered 'cx.exe' }
    if (-not (Test-Path $entered)) { throw "cx.exe not found at $entered" }
    return $entered
}

function Set-TenantEnv($d, [string]$cx) {
    $env:CX_BASE_URI      = $d.baseUri
    $env:CX_BASE_AUTH_URI = $d.baseAuthUri
    $env:CX_TENANT        = $d.tenant
    $env:CX_APIKEY        = $d.apiKey
    $env:CX_CLIENT_ID     = $d.clientId
    $env:CX_CLIENT_SECRET = $d.clientSecret
    $env:CX_CLI_PATH      = Split-Path -Parent $cx
}

# Runs `cx auth validate` with only the given credential type present in the child environment.
function Test-CxConnection($d, [string]$cx, [ValidateSet('apikey', 'client')]$mode) {
    $saved = @{}
    foreach ($n in 'CX_APIKEY', 'CX_CLIENT_ID', 'CX_CLIENT_SECRET') { $saved[$n] = [Environment]::GetEnvironmentVariable($n, 'Process') }
    try {
        if ($mode -eq 'apikey') { $env:CX_CLIENT_ID = ''; $env:CX_CLIENT_SECRET = '' }
        else { $env:CX_APIKEY = '' }
        $out = (cmd /c "`"$cx`" auth validate 2>&1" | Out-String)
        $ok = $out -match 'Successfully authenticated'
        $detail = ($out -split "`r?`n" | Where-Object { $_ -match '\S' } | Select-Object -Last 2) -join ' | '
        return [pscustomobject]@{ Ok = $ok; Detail = $detail }
    }
    finally {
        foreach ($n in $saved.Keys) { [Environment]::SetEnvironmentVariable($n, $saved[$n], 'Process') }
    }
}

# ---------------------------------------------------------------- results
function Show-Results([string]$resultsXml, [string]$logFile, [TimeSpan]$elapsed, [string]$suiteName, [string]$tenantName) {
    Write-Host ""
    Write-Host ('=' * 64) -ForegroundColor Cyan
    Write-Host "  RESULTS  -  $suiteName  on  $tenantName" -ForegroundColor Cyan
    Write-Host ('=' * 64) -ForegroundColor Cyan
    if (-not (Test-Path $resultsXml)) {
        Write-Host '  No testng-results.xml was produced (build/compile error?). See log:' -ForegroundColor Red
        Write-Host "  $logFile"
        return $false
    }
    [xml]$x = Get-Content $resultsXml -Raw
    $r = $x.'testng-results'
    $total = [int]$r.total; $pass = [int]$r.passed; $fail = [int]$r.failed; $skip = [int]$r.skipped
    Write-Host ("  Duration: {0:mm\:ss}`n" -f $elapsed)

    # Per-class matrix (test methods only; data-driven rows count individually)
    $rows = foreach ($c in $x.SelectNodes('//class')) {
        $ms = @($c.SelectNodes('test-method[not(@is-config="true")]'))
        if ($ms.Count -eq 0) { continue }
        [pscustomobject]@{
            Class   = ($c.name -split '\.')[-1]
            Total   = $ms.Count
            Passed  = @($ms | Where-Object status -eq 'PASS').Count
            Failed  = @($ms | Where-Object status -eq 'FAIL').Count
            Skipped = @($ms | Where-Object status -eq 'SKIP').Count
        }
    }
    $rows = @($rows)
    $total = [int]($rows | Measure-Object Total -Sum).Sum; $pass = [int]($rows | Measure-Object Passed -Sum).Sum
    $fail = [int]($rows | Measure-Object Failed -Sum).Sum; $skip = [int]($rows | Measure-Object Skipped -Sum).Sum
    $rows +=[pscustomobject]@{ Class = 'TOTAL'; Total = $total; Passed = $pass; Failed = $fail; Skipped = $skip }
    ($rows | Format-Table Class, Total, Passed, Failed, Skipped -AutoSize | Out-String).TrimEnd() -split "`r?`n" | ForEach-Object { Write-Host "  $_" }

    $failed = @($x.SelectNodes('//test-method[@status="FAIL" and not(@is-config="true")]'))
    if ($failed.Count -gt 0) {
        Write-Host "`n  Failures ($($failed.Count)):" -ForegroundColor Red
        $n = 0
        foreach ($m in $failed) {
            $n++
            $msg = ''
            if ($m.exception -and $m.exception.message) {
                $msg = ($m.exception.message.InnerText -split "`r?`n" | Where-Object { $_ -match '\S' } | Select-Object -First 1)
                if ($msg.Length -gt 150) { $msg = $msg.Substring(0, 150) + '...' }
            }
            $cls = ($m.ParentNode.name -split '\.')[-1]
            $row = ''
            $p = @($m.SelectNodes('params/param/value'))
            if ($p.Count -gt 0) {
                $row = $p[0].InnerText
                if ($row -match 'ScenarioDescription=([^,}]*)') { $row = $Matches[1].Trim() }
                $row = " [$($row.Trim().Substring(0, [Math]::Min(70, $row.Trim().Length)))]"
            }
            Write-Host ("   {0,2}. {1}.{2}{3}" -f $n, $cls, $m.name, $row) -ForegroundColor Red
            if ($msg) { Write-Host ("       {0}" -f $msg) }
        }
    }
    Write-Host "`n  Reports:"
    Write-Host "   Excel : $(Join-Path $root 'test-output\TestReport.xlsx')"
    $html = Get-ChildItem (Join-Path $root 'test-output') -Filter 'ExtentReport_*.html' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($html) { Write-Host "   HTML  : $($html.FullName)" }
    Write-Host "   Log   : $logFile"
    $color = if ($fail -eq 0 -and $total -gt 0) { 'Green' } else { 'Red' }
    Write-Host ("`n  OVERALL: {0}" -f $(if ($fail -eq 0 -and $total -gt 0) { 'PASSED' } else { 'FAILED' })) -ForegroundColor $color
    return ($fail -eq 0 -and $total -gt 0)
}

# ================================================================ main
Write-Host "`nCheckmarx One CLI automation runner" -ForegroundColor Green
$cx = Get-CxPath
Write-Host "Using CLI: $cx"
try { Write-Host ("CLI version: " + ((cmd /c "`"$cx`" version 2>&1" | Out-String).Trim())) } catch { }

# 1) tenant details + connection check (retry until OK or abort)
while ($true) {
    $d = Get-TenantDetails
    Set-TenantEnv $d $cx

    Write-Host "`nChecking connection to $($d.baseAuthUri) (tenant '$($d.tenant)') ..." -ForegroundColor Cyan
    $rApi = Test-CxConnection $d $cx 'apikey'
    $rCli = Test-CxConnection $d $cx 'client'
    Write-Host ("  API key              : {0}" -f $(if ($rApi.Ok) { 'OK' } else { 'FAILED  - ' + $rApi.Detail })) -ForegroundColor $(if ($rApi.Ok) { 'Green' } else { 'Red' })
    Write-Host ("  Client ID + secret   : {0}" -f $(if ($rCli.Ok) { 'OK' } else { 'FAILED  - ' + $rCli.Detail })) -ForegroundColor $(if ($rCli.Ok) { 'Green' } else { 'Red' })

    if ($rApi.Ok -and $rCli.Ok) { break }
    $c = Select-Option 'Connection check failed. What now?' @('Re-enter tenant details', 'Quit')
    if ($c -eq 1) { exit 2 }
}
Write-Host "`nConnection successful." -ForegroundColor Green

# offer to save non-secret details
if (-not $d.fromSaved -and (Read-Host "Save these non-secret details for next time (no secrets stored)? (y/N)") -match "^[yY]") {
    $d.name = Read-Value "Name for this saved tenant" $(if ($d.name) { $d.name } else { $d.tenant })
    New-Item -ItemType Directory -Force $tenantDir | Out-Null
    [pscustomobject]@{ name = $d.name; baseUri = $d.baseUri; baseAuthUri = $d.baseAuthUri; tenant = $d.tenant; clientId = $d.clientId } |
        ConvertTo-Json | Set-Content (Join-Path $tenantDir "$($d.name).json")
    Write-Host 'Saved (no secrets written).'
}

# 2) sanity or regression
$s = Select-Option 'Which suite do you want to run?' @(
    'Sanity      (auth, help, configure, one all-scan-types scan)',
    'Regression  (full suite, ~20 min)')
if ($s -eq 0) { $suiteName = 'Sanity'; $suiteFile = 'testng-sanity.xml' } else { $suiteName = 'Regression'; $suiteFile = 'testng.xml' }


# 3) run
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$logDir = Join-Path $root 'test-output\logs'
New-Item -ItemType Directory -Force $logDir | Out-Null
$logFile = Join-Path $logDir "run_${suiteName}_$stamp.log"
$resultsXml = Join-Path $root 'target\surefire-reports\testng-results.xml'
if (Test-Path $resultsXml) { Remove-Item $resultsXml -Force }   # avoid reading a stale result

Write-Host "`nRunning $suiteName ... (log: $logFile)" -ForegroundColor Cyan
$sw = [Diagnostics.Stopwatch]::StartNew()
$started = 0
# Full output goes to the log file only; the console shows a single progress line.
cmd /c "mvn test -Dsuite.file=$suiteFile 2>&1" | ForEach-Object {
    $_ | Out-File -FilePath $logFile -Append -Encoding utf8
    if ($_ -match '>>> Starting Test') {
        $started++
        Write-Progress -Activity "Running $suiteName on $($d.tenant)" -Status ("{0} tests started - elapsed {1:mm\:ss}" -f $started, $sw.Elapsed)
    }
}
Write-Progress -Activity "Running $suiteName" -Completed
$sw.Stop()

# 4) post results
$ok = Show-Results $resultsXml $logFile $sw.Elapsed $suiteName $d.tenant
exit $(if ($ok) { 0 } else { 1 })
