@echo off
REM ======================================
REM JVA Bookstore - Server Startup Script
REM ======================================

echo [1/3] Setting JAVA_HOME...
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo [2/3] Building project...
cd /d "%~dp0"
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo [3/3] Starting server on port 8081...
echo Server will be available at http://localhost:8081
echo Press Ctrl+C to stop the server
java -Djava.net.preferIPv4Stack=true -jar target\dependency\webapp-runner.jar --port 8081 target\ROOT.war
