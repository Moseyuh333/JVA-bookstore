@echo off
chcp 65001 >nul
echo ========================================
echo   JVA Bookstore - Khoi dong Localhost
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
    echo [2/3] Build project...
    call mvn clean package -DskipTests
) else (
    echo [2/3] Project da duoc build (Bo qua buoc build)
)

REM Khởi động Java server với UTF-8 encoding
echo [3/3] Khoi dong Java server tren port 8081...
echo.
echo ======================================== 
echo   Server se chay tai: http://localhost:8081
echo   Nhan Ctrl+C de dung server
echo ========================================
echo.

java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar target\dependency\webapp-runner.jar --port 8081 target\ROOT.war

pause
