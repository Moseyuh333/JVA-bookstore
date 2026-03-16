@echo off
REM ===================================================
REM JVA Bookstore - Start Server & Tunnel Script
REM ===================================================

echo Starting JVA Bookstore Server and Cloudflare Tunnel...
echo.

REM Set JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM Navigate to project directory
cd /d "%~dp0"

echo Step 1/3: Building project...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Step 2/3: Starting Tomcat server on port 8081...
start "JVA Bookstore Server" /min cmd /c "java -jar target/dependency/webapp-runner.jar --port 8081 target/*.war"

echo Waiting 15 seconds for server to start...
timeout /t 15 /nobreak > nul

echo.
echo Step 3/3: Starting Cloudflare Quick Tunnel...
echo.
echo ===================================================
echo SERVER: http://localhost:8081
echo TUNNEL: The public URL will be displayed below
echo ===================================================
echo.
echo Press Ctrl+C to stop the tunnel (server will keep running)
echo To stop the server, close the "JVA Bookstore Server" window
echo.

cloudflared.exe tunnel --url http://localhost:8081

pause
