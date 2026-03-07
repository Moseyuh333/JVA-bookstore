@echo off
chcp 65001 >nul
echo ========================================
echo   JVA Bookstore - Khoi dong ung dung
echo ========================================
echo.

REM Thiết lập JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM Chuyển đến thư mục dự án
cd /d "%~dp0"

REM Kiểm tra và kill process đang dùng port 8081
echo [1/3] Kiem tra port 8081...
powershell -Command "$p = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue | Select-Object -First 1 ; if ($p) { Stop-Process -Id $p.OwningProcess -Force ; Write-Host '      Da dung process cu' } else { Write-Host '      Port 8081 trong' }"

REM Đợi 2 giây
timeout /t 2 /nobreak >nul

REM Rebuild nếu cần
if not exist "target\ROOT.war" (
    echo [2/4] Build project...
    call mvn clean package -DskipTests
)

REM Khởi động Java server với UTF-8 encoding
echo [2/3] Khoi dong Java server tren port 8081...
start /B java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar target\dependency\webapp-runner.jar --port 8081 target\ROOT.war

REM Đợi 15 giây để server khởi động
echo       Dang doi server khoi dong...
timeout /t 15 /nobreak >nul

REM Kiểm tra server đã chạy chưa
powershell -Command "$p = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue ; if ($p) { Write-Host '      OK Server da san sang!' -ForegroundColor Green } else { Write-Host '      LOI: Server chua khoi dong' -ForegroundColor Red }"

REM Khởi động Cloudflare tunnel
echo [3/3] Khoi dong Cloudflare Tunnel...
echo.
echo ======================================== 
echo   Server: http://localhost:8081
echo   Tunnel URL se hien thi ben duoi:
echo ========================================
echo.
cloudflared.exe tunnel --url http://127.0.0.1:8081

REM Nếu Cloudflare tunnel bị tắt (Ctrl+C), dọn dẹp
echo.
echo Dang dung server...
powershell -Command "$p = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue | Select-Object -First 1 ; if ($p) { Stop-Process -Id $p.OwningProcess -Force ; Write-Host 'Da dung server' }"
pause
