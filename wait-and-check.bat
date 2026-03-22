@echo off
echo Waiting 30 seconds for app to start...
timeout /t 30 /nobreak >nul
echo.
echo Checking if app is running on port 9090...
curl -s -o NUL -w "HTTP Status: %%{http_code}" http://localhost:9090/login
echo.
echo.
echo Checking port 9090...
netstat -an | findstr "9090" | findstr "LISTEN"
echo.
echo Checking login page content...
curl -s http://localhost:9090/login | findstr /i "<title>"
echo.
echo Done checking.

