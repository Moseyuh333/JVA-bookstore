@echo off
REM ===================================================
REM JVA Bookstore - Start Cloudflare Tunnel Script
REM ===================================================

echo Starting Cloudflare Quick Tunnel...
echo.
echo Make sure the server is running on http://localhost:8081 first!
echo.
echo The tunnel URL will be displayed below.
echo Share this URL to make your app accessible from the internet.
echo.
echo Press Ctrl+C to stop the tunnel
echo.

REM Navigate to project directory
cd /d "%~dp0"

REM Start cloudflare tunnel
cloudflared.exe tunnel --url http://localhost:8081

pause
