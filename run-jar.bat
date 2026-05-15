@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

set "JAR_PATH=target\tool-testing-demo-1.0-SNAPSHOT.jar"
set "LOG_DIR=logs"

if not exist "%JAR_PATH%" (
    echo Jar not found: %JAR_PATH%
    exit /b 1
)

if not exist "%LOG_DIR%" (
    mkdir "%LOG_DIR%"
)

for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format ''yyyyMMdd_HHmmss''"') do set "TIMESTAMP=%%i"
set "LOG_FILE=%LOG_DIR%\app_%TIMESTAMP%.log"

echo Starting %JAR_PATH%
echo Log file: %LOG_FILE%

powershell -NoProfile -Command "& { java -jar '%JAR_PATH%' 2>&1 | Tee-Object -FilePath '%LOG_FILE%' }"
set "EXIT_CODE=%ERRORLEVEL%"

echo.
echo Process finished with exit code %EXIT_CODE%
echo Log file: %LOG_FILE%

exit /b %EXIT_CODE%
