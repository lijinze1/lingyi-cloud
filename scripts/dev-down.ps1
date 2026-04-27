$ports = @(18080, 18081, 5173, 5174)
foreach ($port in $ports) {
    $lines = netstat -ano | Select-String ":$port"
    foreach ($line in $lines) {
        $parts = ($line -replace '\s+', ' ').Trim().Split(' ')
        if ($parts.Length -ge 5) {
            $pid = $parts[-1]
            if ($pid -match '^\d+$') {
                try {
                    taskkill /PID $pid /F | Out-Null
                    Write-Host "Stopped PID=$pid on port $port"
                } catch {}
            }
        }
    }
}
