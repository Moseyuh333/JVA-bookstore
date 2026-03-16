@echo off
REM Script to recreate database with new.sql

echo ====================================
echo Recreating JVA Bookstore Database
echo ====================================
echo.

REM Set PostgreSQL path (adjust if needed)
set PGPASSWORD=postgres
set PSQL="C:\Program Files\PostgreSQL\17\bin\psql.exe"
set PGUSER=postgres
set PGHOST=localhost
set PGPORT=5432

echo Step 1: Dropping existing database...
%PSQL% -U %PGUSER% -h %PGHOST% -p %PGPORT% -d postgres -c "DROP DATABASE IF EXISTS jva_bookstore;"
if errorlevel 1 (
    echo Error dropping database!
    pause
    exit /b 1
)
echo ✓ Database dropped

echo.
echo Step 2: Creating new database...
%PSQL% -U %PGUSER% -h %PGHOST% -p %PGPORT% -d postgres -c "CREATE DATABASE jva_bookstore WITH ENCODING='UTF8' LC_COLLATE='en_US.UTF-8' LC_CTYPE='en_US.UTF-8' TEMPLATE=template0;"
if errorlevel 1 (
    echo Error creating database!
    pause
    exit /b 1
)
echo ✓ Database created

echo.
echo Step 3: Importing new.sql...
%PSQL% -U %PGUSER% -h %PGHOST% -p %PGPORT% -d jva_bookstore -f "new.sql"
if errorlevel 1 (
    echo Error importing database!
    pause
    exit /b 1
)
echo ✓ Database imported

echo.
echo ====================================
echo Database setup completed!
echo ====================================
echo.

pause
