$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
Push-Location $repoRoot
try {
    if (-not (Test-Path -LiteralPath '.env')) {
        throw 'Copy .env.example to .env and configure local passwords first.'
    }
    docker compose up -d --wait --wait-timeout 180
    if ($LASTEXITCODE -ne 0) { throw 'MySQL / Redis failed to start.' }
    & "$PSScriptRoot\maven.ps1" package '-DskipTests'
    if ($LASTEXITCODE -ne 0) { throw 'Backend build failed.' }
    & "$env:JAVA_HOME\bin\java.exe" '-Dfile.encoding=UTF-8' '-Duser.timezone=Asia/Shanghai' -jar '.\ruoyi-admin\target\ruoyi-admin.jar'
} finally {
    Pop-Location
}
