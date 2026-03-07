@echo off
REM ==========================================
REM Cloudflare Tunnel - Startup Script
REM ==========================================

cd /d "%~dp0"

echo Starting Cloudflare Tunnel...
echo Connecting to http://127.0.0.1:8081
echo.
echo Your public URL will be displayed below:
echo ==========================================
cloudflared.exe tunnel --url http://127.0.0.1:8081
