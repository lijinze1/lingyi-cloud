$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$pidFile = Join-Path $root ".dev\pids.json"

if (-not (Test-Path $pidFile)) {
    Write-Host "未找到 PID 文件: $pidFile"
    exit 0
}

$content = Get-Content -Raw $pidFile | ConvertFrom-Json

foreach ($p in $content.processes) {
    $pid = [int]$p.pid
    $name = [string]$p.name
    try {
        $proc = Get-Process -Id $pid -ErrorAction Stop
        Stop-Process -Id $pid -Force
        Write-Host "已停止: $name (PID=$pid)"
    }
    catch {
        Write-Host "进程已不存在: $name (PID=$pid)"
    }
}

Remove-Item -LiteralPath $pidFile -Force
Write-Host "已清理 PID 文件。"
