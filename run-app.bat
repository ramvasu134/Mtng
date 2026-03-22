@echo off
cd /d "D:\IntelliJ Projects Trainings\Mtng"
echo Killing old Java processes...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul
echo Starting MTNG App on port 9090...
call mvnw.cmd spring-boot:run

