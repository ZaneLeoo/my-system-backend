$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261018_03_receipt_line_warehouse.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-receipt-line-warehouse.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy receipt line warehouse migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-receipt-line-warehouse.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply receipt line warehouse migration.' }
Write-Host 'Receipt line warehouses are ready. Restart the backend with the matching version.'
