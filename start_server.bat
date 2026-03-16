@echo off
REM ===================================================
REM JVA Bookstore - Start Server Script
REM ===================================================

echo Starting JVA Bookstore Server...
echo.

REM Set JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM Navigate to project directory
cd /d "%~dp0"

echo Building project...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting Tomcat server on port 8081...
echo Server will be available at: http://localhost:8081
echo.
echo Press Ctrl+C to stop the server
echo.

java -jar target/dependency/webapp-runner.jar --port 8081 target/*.war

pause
