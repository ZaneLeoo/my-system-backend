$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261016_01_item.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-item.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy item migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-item.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply item migration.' }
Write-Host 'Item schema and menus are ready. Sign in again to refresh menus.'
