@echo off
echo [MTNG-RUNNER] Starting at %DATE% %TIME% > "D:\IntelliJ Projects Trainings\Mtng\app-status.txt"

:: Kill existing Java
echo [MTNG-RUNNER] Killing Java... >> "D:\IntelliJ Projects Trainings\Mtng\app-status.txt"
taskkill /F /IM java.exe >nul 2>&1
timeout /t 5 /nobreak >nul

:: Check if port 8080 is free
netstat -an | findstr ":8080" | findstr "LISTEN" >> "D:\IntelliJ Projects Trainings\Mtng\app-status.txt"
echo [MTNG-RUNNER] Port status checked >> "D:\IntelliJ Projects Trainings\Mtng\app-status.txt"

:: Start the app
cd /d "D:\IntelliJ Projects Trainings\Mtng"
echo [MTNG-RUNNER] Launching spring-boot:run... >> "D:\IntelliJ Projects Trainings\Mtng\app-status.txt"
call mvnw.cmd spring-boot:run >> "D:\IntelliJ Projects Trainings\Mtng\app-status.txt" 2>&1
echo [MTNG-RUNNER] App exited >> "D:\IntelliJ Projects Trainings\Mtng\app-status.txt"

