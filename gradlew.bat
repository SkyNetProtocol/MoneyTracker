@echo off
setlocal

set GRADLE_VERSION=8.7
set GRADLE_DIR=%USERPROFILE%\.gradle-custom-dist
set GRADLE_HOME=%GRADLE_DIR%\gradle-%GRADLE_VERSION%

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -Command "New-Item -ItemType Directory -Force -Path '%GRADLE_DIR%' | Out-Null; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_DIR%\gradle.zip'; Expand-Archive -Path '%GRADLE_DIR%\gradle.zip' -DestinationPath '%GRADLE_DIR%'; Remove-Item -Path '%GRADLE_DIR%\gradle.zip'"
)

"%GRADLE_HOME%\bin\gradle.bat" %*
