param(
    [ValidateSet("dev", "prod")]
    [string]$Profile = "dev",
    [string[]]$Services = @("lingyi-cart", "lingyi-order", "lingyi-payment", "lingyi-product", "lingyi-seckill"),
    [string]$NacosAddr = "127.0.0.1:8848",
    [string]$Username = "nacos",
    [string]$Password = "nacos",
    [string]$Namespace
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$configDir = Join-Path $root "lingyi-config\src\main\resources\nacos\$Profile"

if (-not $Namespace) {
    $Namespace = if ($Profile -eq "dev") {
        "27119826-e1e3-481e-a1db-25eddb7f2327"
    }
    else {
        "1fac8ea3-a23c-4f2d-a126-2542a7081ced"
    }
}

$headers = @{
    username = $Username
    password = $Password
}

foreach ($service in $Services) {
    $dataId = "$service-$Profile.yaml"
    $path = Join-Path $configDir $dataId
    if (-not (Test-Path $path)) {
        throw "Config file not found: $path"
    }

    $content = Get-Content -Raw -LiteralPath $path
    $body = @{
        tenant  = $Namespace
        group   = "DEFAULT_GROUP"
        dataId  = $dataId
        type    = "yaml"
        content = $content
    }

    $response = Invoke-RestMethod `
        -Method Post `
        -Uri "http://$NacosAddr/nacos/v1/cs/configs" `
        -Headers $headers `
        -ContentType "application/x-www-form-urlencoded; charset=UTF-8" `
        -Body $body

    Write-Host "Published $dataId => $response"
}
