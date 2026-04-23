param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$runDir = Join-Path $root ".dev"
$logDir = Join-Path $runDir "logs"
$pidFile = Join-Path $runDir "pids.json"

New-Item -ItemType Directory -Force -Path $runDir | Out-Null
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

function Start-LoggedProcess {
    param(
        [string]$Name,
        [string]$WorkDir,
        [string]$Command
    )

    $outLog = Join-Path $logDir "$Name.out.log"
    $errLog = Join-Path $logDir "$Name.err.log"
    if (Test-Path $outLog) { Remove-Item -LiteralPath $outLog -Force }
    if (Test-Path $errLog) { Remove-Item -LiteralPath $errLog -Force }

    $proc = Start-Process -FilePath "powershell.exe" `
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $Command) `
        -WorkingDirectory $WorkDir `
        -RedirectStandardOutput $outLog `
        -RedirectStandardError $errLog `
        -PassThru

    return @{
        name = $Name
        pid = $proc.Id
        out = $outLog
        err = $errLog
        cwd = $WorkDir
    }
}

Write-Host "==> Root: $root"

if (-not $SkipBuild) {
    Write-Host "==> 编译后端（common + user + gateway）..."
    Push-Location $root
    try {
        mvn.cmd -DskipTests -pl lingyi-common/lingyi-common-core,lingyi-common/lingyi-common-web,lingyi-service/lingyi-user,lingyi-gateway/lingyi-gateway-server compile
    }
    finally {
        Pop-Location
    }
}
else {
    Write-Host "==> 跳过编译（-SkipBuild）"
}

$commonEnv = @"
`$env:NACOS_ADDR='127.0.0.1:8848';
`$env:NACOS_USERNAME='nacos';
`$env:NACOS_PASSWORD='nacos';
`$env:SPRING_PROFILES_ACTIVE='dev';
`$env:MYSQL_HOST='127.0.0.1';
`$env:MYSQL_PORT='3306';
`$env:MYSQL_DB='lingyi';
`$env:MYSQL_USERNAME='root';
`$env:MYSQL_PASSWORD='123456';
`$env:JWT_SECRET='lingyi-dev-secret-please-change';
"@

$processes = @()

Write-Host "==> 启动 user 服务..."
$userCmd = @"
$commonEnv
Set-Location '$root';
mvn.cmd -pl lingyi-service/lingyi-user -DskipTests spring-boot:run
"@
$processes += Start-LoggedProcess -Name "user" -WorkDir $root -Command $userCmd

Write-Host "==> 启动 gateway 服务..."
$gatewayCmd = @"
$commonEnv
Set-Location '$root';
mvn.cmd -pl lingyi-gateway/lingyi-gateway-server -DskipTests spring-boot:run
"@
$processes += Start-LoggedProcess -Name "gateway" -WorkDir $root -Command $gatewayCmd

Write-Host "==> 启动 C端前端..."
$frontendCwd = Join-Path $root "frontend"
$webCCmd = @"
Set-Location '$frontendCwd';
npm.cmd run dev:c
"@
$processes += Start-LoggedProcess -Name "web-c" -WorkDir $frontendCwd -Command $webCCmd

Write-Host "==> 启动 B端前端..."
$webBCmd = @"
Set-Location '$frontendCwd';
npm.cmd run dev:b
"@
$processes += Start-LoggedProcess -Name "web-b" -WorkDir $frontendCwd -Command $webBCmd

$payload = @{
    startedAt = (Get-Date).ToString("s")
    processes = $processes
}

$payload | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 $pidFile

Write-Host ""
Write-Host "启动完成。"
Write-Host "PID 文件: $pidFile"
Write-Host "日志目录: $logDir"
Write-Host "C端: http://localhost:5173"
Write-Host "B端: http://localhost:5174"
Write-Host "Gateway: http://localhost:18080"
