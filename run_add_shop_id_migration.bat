@echo off
REM Script để chạy migration thêm shop_id vào bảng orders trên Heroku
REM Cách sử dụng: run_add_shop_id_migration.bat [app-name]

SET APP_NAME=%1

IF "%APP_NAME%"=="" (
    echo Error: Chua cung cap ten app Heroku
    echo Su dung: run_add_shop_id_migration.bat [app-name]
    echo Vi du: run_add_shop_id_migration.bat jva-bookstore-app
    exit /b 1
)

echo ========================================
echo Chay migration: Them shop_id vao orders
echo App: %APP_NAME%
echo ========================================
echo.

echo [1/3] Kiem tra ket noi database...
heroku pg:info --app %APP_NAME%
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Khong the ket noi den database
    exit /b 1
)

echo.
echo [2/3] Chay migration SQL...
heroku pg:psql --app %APP_NAME% < add_shop_id_to_orders.sql

IF %ERRORLEVEL% NEQ 0 (
    echo Error: Migration that bai!
    exit /b 1
)

echo.
echo [3/3] Xac nhan ket qua...
echo Chay query kiem tra:
heroku pg:psql --app %APP_NAME% --command "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'orders' AND column_name = 'shop_id';"

echo.
echo ========================================
echo Migration hoan tat!
echo ========================================
echo.
echo Kiem tra du lieu:
heroku pg:psql --app %APP_NAME% --command "SELECT COUNT(*) as total, COUNT(shop_id) as with_shop FROM orders;"

pause
