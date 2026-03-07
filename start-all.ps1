# ======================================================
# JVA Bookstore - Complete Startup Script (Server + Tunnel)
# ======================================================

$ErrorActionPreference = "Stop"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JVA Bookstore - Complete Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME
Write-Host "[1/5] Setting JAVA_HOME..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "      JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray

# Build project
Write-Host "[2/5] Building project with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    pause
    exit 1
}

# Start Java server in background
Write-Host "[3/5] Starting Java server on port 8081..." -ForegroundColor Yellow
$ServerJob = Start-Job -ScriptBlock {
    param($JavaHome, $ProjectDir)
    $env:JAVA_HOME = $JavaHome
    Set-Location $ProjectDir
    & java -Djava.net.preferIPv4Stack=true -jar target\dependency\webapp-runner.jar --port 8081 target\ROOT.war
} -ArgumentList $env:JAVA_HOME, $ProjectDir

# Wait for server to start
Write-Host "[4/5] Waiting for server startup (15 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Check if server is running
$ServerRunning = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
if ($ServerRunning) {
    Write-Host "      Server is UP and listening on port 8081" -ForegroundColor Green
} else {
    Write-Host "      WARNING: Server may not be ready yet" -ForegroundColor Yellow
}

# Start Cloudflare tunnel
Write-Host "[5/5] Starting Cloudflare Tunnel..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Your public URL will appear below:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

& .\cloudflared.exe tunnel --url http://127.0.0.1:8081

# Cleanup on exit (Ctrl+C)
Write-Host ""
Write-Host "Stopping server..." -ForegroundColor Yellow
Stop-Job -Job $ServerJob
Remove-Job -Job $ServerJob
