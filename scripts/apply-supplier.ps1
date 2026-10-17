$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261017_01_supplier.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-supplier.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy supplier migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-supplier.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply supplier migration.' }
Write-Host 'Supplier schema and menus are ready. Sign in again to refresh menus.'
