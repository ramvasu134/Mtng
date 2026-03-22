@echo off
echo ============================================
echo   MTNG - Verify Application
echo ============================================
echo.
echo Checking port 8080...
netstat -an | findstr ":8080" | findstr "LISTEN"
if errorlevel 1 (
    echo   Port 8080 is FREE - no app running.
) else (
    echo   Port 8080 is IN USE - app may be running!
)
echo.
echo Trying to reach login page...
curl -s -o NUL -w "  HTTP Status: %%{http_code}" http://localhost:8080/login 2>nul
echo.
echo.
echo Trying to reach students page...
curl -s -o NUL -w "  HTTP Status: %%{http_code}" http://localhost:8080/students 2>nul
echo.
echo.
echo Trying to reach H2 console...
curl -s -o NUL -w "  HTTP Status: %%{http_code}" http://localhost:8080/h2-console 2>nul
echo.
echo.
echo ============================================
echo Done. Press any key to exit.
pause >nul

