$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261017_02_purchase_order.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-purchase-order.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy purchase-order migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-purchase-order.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply purchase-order migration.' }
Write-Host 'Purchase order schema and menus are ready. Sign in again to refresh menus.'
