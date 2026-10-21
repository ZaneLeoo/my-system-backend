param([string]$Java8Home = $env:MY_SYSTEM_JAVA8_HOME, [switch]$Https)
$ErrorActionPreference = 'Stop'
$signingRepo = Split-Path -Parent $PSScriptRoot
if (-not $Java8Home) { throw 'Set MY_SYSTEM_JAVA8_HOME or pass -Java8Home to an installed Java 8 runtime.' }
$signingJava = Join-Path $Java8Home 'bin/java.exe'
if (-not (Test-Path -LiteralPath $signingJava)) { throw 'Java 8 executable not found.' }
& "$PSScriptRoot/maven.ps1" -pl my-system-signing test-compile dependency:build-classpath '-Dmdep.outputFile=target/java8-test-classpath.txt' '-Dmdep.includeScope=test'
if ($LASTEXITCODE -ne 0) { throw 'Failed to compile compatibility tests.' }
$signingModule = Join-Path $signingRepo 'my-system-signing'
$signingDeps = (Get-Content -LiteralPath "$signingModule/target/java8-test-classpath.txt" -Raw).Trim()
$signingClasspath = "$signingModule/target/classes;$signingModule/target/test-classes;$signingDeps"
& $signingJava -cp $signingClasspath org.junit.runner.JUnitCore com.ruoyi.signing.SigningCoreTest com.ruoyi.signing.SigningSealTest
if ($LASTEXITCODE -ne 0) { throw 'Java 8 unit tests failed.' }
$signingSmokeArgs = @('-cp', $signingClasspath, 'com.ruoyi.signing.SigningJava8Smoke')
if ($Https) { $signingSmokeArgs += '--https' }
& $signingJava @signingSmokeArgs
if ($LASTEXITCODE -ne 0) { throw 'Java 8 runtime compatibility check failed.' }
