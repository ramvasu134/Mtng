@echo off
timeout /t 40 /nobreak >nul
echo [CHECK] Checking at %DATE% %TIME% > "D:\IntelliJ Projects Trainings\Mtng\verify-status.txt"
netstat -an | findstr ":8080" | findstr "LISTEN" >> "D:\IntelliJ Projects Trainings\Mtng\verify-status.txt"
curl -s -m 5 -o NUL -w "HTTP_CODE:%%{http_code}" http://localhost:8080/login >> "D:\IntelliJ Projects Trainings\Mtng\verify-status.txt" 2>&1
curl -s -m 5 http://localhost:8080/login >> "D:\IntelliJ Projects Trainings\Mtng\verify-status.txt" 2>&1
echo. >> "D:\IntelliJ Projects Trainings\Mtng\verify-status.txt"
echo [DONE] >> "D:\IntelliJ Projects Trainings\Mtng\verify-status.txt"

