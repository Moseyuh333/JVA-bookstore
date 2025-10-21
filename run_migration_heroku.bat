@echo off
echo ====================================
echo HEROKU DATABASE MIGRATION
echo ====================================
echo.
echo Step 1: Login to Heroku (if not logged in)
heroku login
echo.
echo Step 2: Run migration on Heroku Postgres
heroku pg:psql -a jva-bookstore-17d2d34519f8 < apply_migration.sql
echo.
echo Migration completed!
pause
