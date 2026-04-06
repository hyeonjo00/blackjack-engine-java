$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$sourceRoot = Join-Path $projectRoot "src\main\java"
$outDir = Join-Path $projectRoot "out"

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java |
    Select-Object -ExpandProperty FullName

if (-not $sources) {
    throw "No Java source files were found under $sourceRoot."
}

javac -d $outDir $sources

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $outDir com.blackjackengine.Main
exit $LASTEXITCODE

