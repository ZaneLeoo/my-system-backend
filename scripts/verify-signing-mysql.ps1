$ErrorActionPreference = 'Stop'
$signingRoot = Split-Path -Parent $PSScriptRoot
$signingDatabase = 'my_system_signing_test_' + [Guid]::NewGuid().ToString('N').Substring(0,8)
if ($signingDatabase -notmatch '^my_system_signing_test_[a-f0-9]{8}$') { throw 'Invalid isolated test database name.' }
$signingLocalConfig = @{}
foreach ($signingLine in (Get-Content -LiteralPath "$signingRoot/.env")) {
    if ($signingLine -match '^([A-Z0-9_]+)=(.*)$') { $signingLocalConfig[$Matches[1]] = $Matches[2].Trim().Trim('"').Trim("'") }
}
$signingPort = if ($signingLocalConfig.MYSQL_PORT) { $signingLocalConfig.MYSQL_PORT } else { '13306' }
$signingCreated = $false
try {
    docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root -e "$1"' sh "CREATE DATABASE $signingDatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    if ($LASTEXITCODE -ne 0) { throw 'Failed to create isolated signing test database.' }
    $signingCreated = $true
    $env:SIGNING_TEST_URL = "jdbc:mysql://127.0.0.1:$signingPort/${signingDatabase}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
    $env:SIGNING_TEST_USER = 'root'
    $env:SIGNING_TEST_PASSWORD = $signingLocalConfig.MYSQL_ROOT_PASSWORD
    & "$PSScriptRoot/maven.ps1" -pl my-system-business -am test '-Dtest=SigningIntegrationTest' '-Dsurefire.failIfNoSpecifiedTests=false'
    if ($LASTEXITCODE -ne 0) { throw 'Signing MySQL integration tests failed.' }
} finally {
    Remove-Item Env:SIGNING_TEST_URL,Env:SIGNING_TEST_USER,Env:SIGNING_TEST_PASSWORD -ErrorAction SilentlyContinue
    if ($signingCreated -and $signingDatabase -match '^my_system_signing_test_[a-f0-9]{8}$') {
        docker exec my-system-mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root -e "$1"' sh "DROP DATABASE $signingDatabase"
        if ($LASTEXITCODE -ne 0) { Write-Warning "Failed to remove isolated test database: $signingDatabase" }
    }
}
