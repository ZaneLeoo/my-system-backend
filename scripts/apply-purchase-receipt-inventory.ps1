$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261018_02_purchase_receipt_inventory.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-purchase-receipt-inventory.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy purchase-receipt-inventory migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-purchase-receipt-inventory.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply purchase-receipt-inventory migration.' }
Write-Host 'Purchase receipt and inventory schema and menus are ready. Sign in again to refresh menus.'
