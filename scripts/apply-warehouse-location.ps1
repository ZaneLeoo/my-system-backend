$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261017_03_warehouse_location.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-warehouse-location.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy warehouse-location migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-warehouse-location.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply warehouse-location migration.' }
Write-Host 'Warehouse and location schema and menus are ready. Sign in again to refresh menus.'
