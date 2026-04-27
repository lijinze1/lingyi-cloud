param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

if (-not $SkipBuild) {
    Write-Host "==> Compile backend modules (common + user + gateway)..."
    Push-Location $root
    try {
        mvn.cmd -DskipTests -pl lingyi-common/lingyi-common-core,lingyi-common/lingyi-common-web,lingyi-service/lingyi-user,lingyi-gateway/lingyi-gateway-server compile
    }
    finally {
        Pop-Location
    }
}
else {
    Write-Host "==> Skip build (-SkipBuild)"
}

$envCmd = "$env:NACOS_ADDR='127.0.0.1:8848';$env:NACOS_USERNAME='nacos';$env:NACOS_PASSWORD='nacos';$env:SPRING_PROFILES_ACTIVE='dev';$env:MYSQL_HOST='127.0.0.1';$env:MYSQL_PORT='3306';$env:MYSQL_DB='lingyi';$env:MYSQL_USERNAME='root';$env:MYSQL_PASSWORD='123456';$env:JWT_SECRET='lingyi-dev-secret-please-change';"

Write-Host "==> Start user service..."
cmd.exe /c "start \"lingyi-user\" /min powershell -NoProfile -ExecutionPolicy Bypass -Command \"$envCmd Set-Location '$root'; mvn.cmd -pl lingyi-service/lingyi-user -DskipTests spring-boot:run\""

Write-Host "==> Start gateway service..."
cmd.exe /c "start \"lingyi-gateway\" /min powershell -NoProfile -ExecutionPolicy Bypass -Command \"$envCmd Set-Location '$root'; mvn.cmd -pl lingyi-gateway/lingyi-gateway-server -DskipTests spring-boot:run\""

Write-Host "==> Start C frontend..."
cmd.exe /c "start \"lingyi-web-c\" /min powershell -NoProfile -ExecutionPolicy Bypass -Command \"Set-Location '$root\\frontend'; npm.cmd run dev:c\""

Write-Host "==> Start B frontend..."
cmd.exe /c "start \"lingyi-web-b\" /min powershell -NoProfile -ExecutionPolicy Bypass -Command \"Set-Location '$root\\frontend'; npm.cmd run dev:b\""

Write-Host ""
Write-Host "All processes dispatched."
Write-Host "C frontend: http://localhost:5173"
Write-Host "B frontend: http://localhost:5174"
Write-Host "Gateway: http://localhost:18080"
Write-Host "User service: http://localhost:18081"
