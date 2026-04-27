param(
    [long]$UserId = 1000000000002,
    [long]$ActivityId = 6001,
    [string]$MySqlExe = "F:\Mysql\mysql-8.0.32-winx64\bin\mysql.exe",
    [string]$MySqlHost = "127.0.0.1",
    [int]$MySqlPort = 3306,
    [string]$MySqlUser = "root",
    [string]$MySqlPassword = "123456",
    [string]$Database = "lingyi",
    [string]$RedisHost = "127.0.0.1",
    [int]$RedisPort = 6379,
    [string]$RedisPassword = ""
)

$ErrorActionPreference = "Stop"

function Invoke-MySqlRaw {
    param([string]$Sql)
    & $MySqlExe --host=$MySqlHost --port=$MySqlPort --user=$MySqlUser --password=$MySqlPassword $Database --batch --skip-column-names -e $Sql
}

function Read-RedisResponse {
    param(
        [System.IO.Stream]$Stream
    )

    $prefix = $Stream.ReadByte()
    if ($prefix -lt 0) {
        throw "Redis connection closed unexpectedly."
    }

    function Read-Line([System.IO.Stream]$InnerStream) {
        $bytes = New-Object System.Collections.Generic.List[byte]
        while ($true) {
            $b = $InnerStream.ReadByte()
            if ($b -lt 0) {
                throw "Redis response truncated."
            }
            if ($b -eq 13) {
                $next = $InnerStream.ReadByte()
                if ($next -ne 10) {
                    throw "Invalid Redis line ending."
                }
                break
            }
            $bytes.Add([byte]$b)
        }
        return [System.Text.Encoding]::UTF8.GetString($bytes.ToArray())
    }

    switch ([char]$prefix) {
        '+' { return Read-Line $Stream }
        '-' { throw (Read-Line $Stream) }
        ':' { return [long](Read-Line $Stream) }
        '$' {
            $length = [int](Read-Line $Stream)
            if ($length -lt 0) {
                return $null
            }
            $buffer = New-Object byte[] $length
            $offset = 0
            while ($offset -lt $length) {
                $read = $Stream.Read($buffer, $offset, $length - $offset)
                if ($read -le 0) {
                    throw "Redis bulk string truncated."
                }
                $offset += $read
            }
            [void]$Stream.ReadByte()
            [void]$Stream.ReadByte()
            return [System.Text.Encoding]::UTF8.GetString($buffer)
        }
        default {
            throw "Unsupported Redis response prefix: $([char]$prefix)"
        }
    }
}

function Invoke-RedisCommand {
    param(
        [System.IO.Stream]$Stream,
        [string[]]$Args
    )

    $builder = New-Object System.Text.StringBuilder
    [void]$builder.Append("*$($Args.Count)`r`n")
    foreach ($arg in $Args) {
        $value = [string]$arg
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($value)
        [void]$builder.Append("$" + $bytes.Length + "`r`n")
        [void]$builder.Append($value + "`r`n")
    }
    $payload = [System.Text.Encoding]::UTF8.GetBytes($builder.ToString())
    $Stream.Write($payload, 0, $payload.Length)
    $Stream.Flush()
    return Read-RedisResponse -Stream $Stream
}

$cleanupSql = @"
UPDATE ly_payment
SET is_deleted = 1, updated_at = NOW()
WHERE is_deleted = 0
  AND order_no IN (
      SELECT order_no FROM (
          SELECT order_no
          FROM ly_order
          WHERE user_id = $UserId AND order_type = 1 AND is_deleted = 0
      ) t
  );

UPDATE ly_order_log
SET is_deleted = 1, updated_at = NOW()
WHERE is_deleted = 0
  AND order_id IN (
      SELECT id FROM (
          SELECT id
          FROM ly_order
          WHERE user_id = $UserId AND order_type = 1 AND is_deleted = 0
      ) t
  );

UPDATE ly_order_item
SET is_deleted = 1, updated_at = NOW()
WHERE is_deleted = 0
  AND order_id IN (
      SELECT id FROM (
          SELECT id
          FROM ly_order
          WHERE user_id = $UserId AND order_type = 1 AND is_deleted = 0
      ) t
  );

UPDATE ly_order
SET is_deleted = 1, updated_at = NOW(), status = 50, pay_status = 0
WHERE user_id = $UserId AND order_type = 1 AND is_deleted = 0;

UPDATE ly_seckill_record
SET is_deleted = 1, updated_at = NOW(), status = 4, order_id = NULL
WHERE user_id = $UserId AND activity_id = $ActivityId AND is_deleted = 0;

UPDATE ly_activity_sku
SET stock_available = stock_total, stock_locked = 0, updated_at = NOW()
WHERE activity_id = $ActivityId AND is_deleted = 0;
"@

Invoke-MySqlRaw $cleanupSql | Out-Null

$stockRows = Invoke-MySqlRaw "SELECT id, stock_available FROM ly_activity_sku WHERE activity_id = $ActivityId AND is_deleted = 0 ORDER BY id;"

$client = [System.Net.Sockets.TcpClient]::new()
$client.Connect($RedisHost, $RedisPort)
$stream = $client.GetStream()

try {
    if ($RedisPassword) {
        [void](Invoke-RedisCommand -Stream $stream -Args @("AUTH", $RedisPassword))
    }
    foreach ($row in $stockRows) {
        if (-not $row) {
            continue
        }
        $parts = $row -split "\t"
        if ($parts.Count -lt 2) {
            continue
        }
        $activitySkuId = $parts[0]
        $stockAvailable = $parts[1]
        [void](Invoke-RedisCommand -Stream $stream -Args @("DEL", "lingyi:seckill:user:${activitySkuId}:${UserId}"))
        [void](Invoke-RedisCommand -Stream $stream -Args @("SET", "lingyi:seckill:stock:${activitySkuId}", $stockAvailable))
    }
}
finally {
    $stream.Dispose()
    $client.Dispose()
}

Write-Output "Seckill demo data reset completed for user $UserId and activity $ActivityId."
