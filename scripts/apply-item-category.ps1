$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261014_01_item_category.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-item-category.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy item category migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-item-category.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply item category migration.' }
Write-Host 'Item category schema and menus are ready. Sign in again to refresh menus.'
