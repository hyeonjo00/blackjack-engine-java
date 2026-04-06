param(
    [switch]$CompileOnly
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$javaSourceRoot = Join-Path $projectRoot "src\main\java"
$resourceRoot = Join-Path $projectRoot "src\main\resources"
$outDir = Join-Path $projectRoot "out"
$javaFxVersion = "21.0.10"
$javaFxLibDir = Join-Path $projectRoot ".javafx-cache\$javaFxVersion\lib"

function Get-JavaSources {
    param(
        [string[]]$Roots
    )

    $allSources = @()

    foreach ($root in $Roots) {
        if (Test-Path $root) {
            $allSources += Get-ChildItem -Path $root -Recurse -Filter *.java |
                Select-Object -ExpandProperty FullName
        }
    }

    return $allSources
}

function Ensure-JavaFxRuntime {
    $artifacts = @(
        "javafx-base",
        "javafx-graphics",
        "javafx-controls",
        "javafx-fxml",
        "javafx-media",
        "javafx-web"
    )

    $baseUrl = "https://repo.maven.apache.org/maven2/org/openjfx"

    if (-not (Test-Path $javaFxLibDir)) {
        New-Item -ItemType Directory -Path $javaFxLibDir -Force | Out-Null
    }

    foreach ($artifact in $artifacts) {
        foreach ($suffix in @("", "-win")) {
            $fileName = "$artifact-$javaFxVersion$suffix.jar"
            $destination = Join-Path $javaFxLibDir $fileName

            if (Test-Path $destination) {
                continue
            }

            $url = "$baseUrl/$artifact/$javaFxVersion/$fileName"
            Write-Host "Downloading $fileName..."
            Invoke-WebRequest -Uri $url -OutFile $destination
        }
    }
}

function Copy-UiResources {
    if (Test-Path $resourceRoot) {
        Copy-Item -Path (Join-Path $resourceRoot "*") -Destination $outDir -Recurse -Force
    }
}

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

Ensure-JavaFxRuntime

$sources = Get-JavaSources -Roots @($javaSourceRoot)

if (-not $sources) {
    throw "No Java source files were found under $javaSourceRoot."
}

if (-not (Get-ChildItem -Path $javaFxLibDir -Filter *.jar -ErrorAction SilentlyContinue)) {
    throw "JavaFX jars were not found under $javaFxLibDir."
}

javac --module-path $javaFxLibDir --add-modules javafx.controls,javafx.fxml,javafx.web -d $outDir $sources

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Copy-UiResources

if ($CompileOnly) {
    exit 0
}

java --module-path $javaFxLibDir --add-modules javafx.controls,javafx.fxml,javafx.web -cp $outDir com.blackjackengine.ui.BlackjackApplication
exit $LASTEXITCODE
