$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261016_02_reference.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-reference.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy reference migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-reference.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply reference migration.' }
Write-Host 'Reference permissions are ready. Sign in again to refresh permissions.'
