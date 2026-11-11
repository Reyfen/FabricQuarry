$ErrorActionPreference = 'Stop'

$appHome = $PSScriptRoot
$wrapperJar = Join-Path $appHome 'gradle\wrapper\gradle-wrapper.jar'
if (-not (Test-Path $wrapperJar)) {
    Write-Error "Gradle wrapper jar not found: $wrapperJar"
    exit 1
}

if ($env:JAVA_HOME) {
    $java = Join-Path $env:JAVA_HOME 'bin\java.exe'
    if (-not (Test-Path $java)) {
        Write-Error "JAVA_HOME is set to an invalid directory: $env:JAVA_HOME"
        exit 1
    }
} else {
    $command = Get-Command java.exe -ErrorAction SilentlyContinue
    if (-not $command) {
        Write-Error 'JAVA_HOME is not set and no java.exe was found on PATH'
        exit 1
    }
    $java = $command.Source
}

$jvmArgs = @('-Xmx64m', '-Xms64m')
foreach ($options in @($env:JAVA_OPTS, $env:GRADLE_OPTS)) {
    if ($options) {
        $jvmArgs += $options -split '\s+' | Where-Object { $_ }
    }
}

& $java @jvmArgs "-Dorg.gradle.appname=gradlew" -classpath $wrapperJar org.gradle.wrapper.GradleWrapperMain @args
exit $LASTEXITCODE
