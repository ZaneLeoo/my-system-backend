param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArguments
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$javaCandidates = @($env:MY_SYSTEM_JAVA_HOME, $env:JAVA_HOME, $env:JAVA_HOME_17_X64)
$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
if ($javaCommand) {
    $javaCandidates += Split-Path -Parent (Split-Path -Parent $javaCommand.Source)
}
foreach ($installRoot in @('C:\APP\Dev\Env\Java', 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java')) {
    if (Test-Path -LiteralPath $installRoot) {
        $javaCandidates += Get-ChildItem -LiteralPath $installRoot -Filter java.exe -File -Recurse |
            ForEach-Object { Split-Path -Parent (Split-Path -Parent $_.FullName) }
    }
}
$selectedJava = $null
foreach ($candidate in ($javaCandidates | Select-Object -Unique)) {
    if ($candidate -and (Test-Path -LiteralPath "$candidate\bin\javac.exe")) {
        $releaseFile = Join-Path $candidate 'release'
        $javaInfo = if (Test-Path -LiteralPath $releaseFile) { Get-Content -LiteralPath $releaseFile -Raw } else { '' }
        if ($javaInfo -match 'JAVA_VERSION="(\d+)' -and [int]$Matches[1] -ge 17) {
            $selectedJava = $candidate
            break
        }
    }
}
if (-not $selectedJava) {
    throw 'JDK 17+ is required. Set MY_SYSTEM_JAVA_HOME to your JDK installation.'
}
$env:JAVA_HOME = $selectedJava
$env:Path = "$selectedJava\bin;$env:Path"
Push-Location $repoRoot
try {
    & "$repoRoot\mvnw.cmd" @MavenArguments
    $mavenExitCode = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $mavenExitCode
