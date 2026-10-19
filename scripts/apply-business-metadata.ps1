$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$migrationPath = Join-Path $root 'sql/migrations/20261019_03_business_metadata.sql'
docker cp $migrationPath 'my-system-mysql:/tmp/my-system-business-metadata.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to copy business metadata migration.' }
docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-business-metadata.sql'
if ($LASTEXITCODE -ne 0) { throw 'Failed to apply business metadata migration.' }
Write-Host 'Business metadata catalog is ready; query publications and business data are unchanged.'
