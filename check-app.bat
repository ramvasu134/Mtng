@echo off
curl -s -o NUL -w "HTTP_STATUS:%%{http_code}" http://localhost:9090/login 2>nul
echo.
echo ---
netstat -an | findstr ":9090" | findstr "LISTENING"
echo ---
netstat -an | findstr ":8080" | findstr "LISTENING"

