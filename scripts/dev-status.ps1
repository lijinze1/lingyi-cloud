$checks = @(
    @{ name = 'gateway'; port = 18080 },
    @{ name = 'user'; port = 18081 },
    @{ name = 'web-c'; port = 5173 },
    @{ name = 'web-b'; port = 5174 }
)

foreach ($c in $checks) {
    $exists = netstat -ano | Select-String (":" + $c.port + " ")
    if ($exists) {
        Write-Host ("{0,-10} port={1} status=RUNNING" -f $c.name, $c.port)
    } else {
        Write-Host ("{0,-10} port={1} status=STOPPED" -f $c.name, $c.port)
    }
}
