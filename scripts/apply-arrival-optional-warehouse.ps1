$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261018_04_arrival_optional_warehouse.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-arrival-optional-warehouse.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy optional arrival warehouse migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-arrival-optional-warehouse.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply optional arrival warehouse migration.' }
Write-Host 'The expected warehouse on purchase arrivals is now optional.'
