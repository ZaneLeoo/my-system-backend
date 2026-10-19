$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
foreach ($migration in @('20261019_01_query_framework.sql', '20261019_02_query_navigation.sql')) {
  $migrationPath = Join-Path $root "sql/migrations/$migration"
  docker cp $migrationPath 'my-system-mysql:/tmp/my-system-query-framework.sql'
  if ($LASTEXITCODE -ne 0) { throw "Failed to copy $migration." }
  docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 -u root "$MYSQL_DATABASE" < /tmp/my-system-query-framework.sql'
  if ($LASTEXITCODE -ne 0) { throw "Failed to apply $migration." }
}
Write-Host 'Query catalog, initial publications and permissions are ready.'
