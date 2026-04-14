@echo off
REM ==========================================
REM Cloudflare Tunnel - Startup Script
REM ==========================================

cd /d "%~dp0"

set PORT=%~1
if "%PORT%"=="" set PORT=8082

echo Starting Cloudflare Tunnel...
echo Connecting to http://127.0.0.1:%PORT%
echo.
echo Your public URL will be displayed below:
echo ==========================================
cloudflared.exe tunnel --url http://127.0.0.1:%PORT%
