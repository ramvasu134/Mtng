# Run MTNG Application - PowerShell Script
# Execute from any PowerShell terminal: powershell -NoProfile -File "D:\IntelliJ Projects Trainings\Mtng\run.ps1"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  MTNG - Meeting Management Platform" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Set-Location "D:\IntelliJ Projects Trainings\Mtng"

Write-Host "[1] Killing existing Java processes..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3
Write-Host "    Done." -ForegroundColor Green
Write-Host ""

Write-Host "[2] Starting MTNG on port 8080..." -ForegroundColor Yellow
Write-Host ""
Write-Host "    App URL:      http://localhost:8080" -ForegroundColor White
Write-Host "    Login ADMIN:  admin / admin123" -ForegroundColor White
Write-Host "    Login USER:   user  / user123" -ForegroundColor White
Write-Host "    H2 Console:   http://localhost:8080/h2-console" -ForegroundColor White
Write-Host "    H2 JDBC URL:  jdbc:h2:mem:mtngdb" -ForegroundColor White
Write-Host "    H2 User: sa   Password: (empty)" -ForegroundColor White
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

& .\mvnw.cmd spring-boot:run

