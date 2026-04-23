$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$pidFile = Join-Path $root ".dev\pids.json"

if (-not (Test-Path $pidFile)) {
    Write-Host "未找到 PID 文件: $pidFile"
    Write-Host "可执行: powershell -ExecutionPolicy Bypass -File .\\scripts\\dev-up.ps1"
    exit 0
}

$content = Get-Content -Raw $pidFile | ConvertFrom-Json
Write-Host "Started At: $($content.startedAt)"
Write-Host ""

foreach ($p in $content.processes) {
    $pid = [int]$p.pid
    $name = [string]$p.name
    try {
        $proc = Get-Process -Id $pid -ErrorAction Stop
        Write-Host ("{0,-10} PID={1} STATUS=RUNNING" -f $name, $pid)
    }
    catch {
        Write-Host ("{0,-10} PID={1} STATUS=STOPPED" -f $name, $pid)
    }
}

Write-Host ""
Write-Host "日志目录: $root\\.dev\\logs"
