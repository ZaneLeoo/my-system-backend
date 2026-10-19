$ErrorActionPreference = 'Stop'
$migrationPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'sql/migrations/20261019_01_query_framework.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-query-framework.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy query framework migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-query-framework.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply query framework migration.' }
Write-Host 'Query catalog, initial publications and permissions are ready.'
