$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261015_01_unit.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-unit.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy unit migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-unit.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply unit migration.' }
Write-Host 'Unit schema, initial units and menus are ready. Sign in again to refresh menus.'
