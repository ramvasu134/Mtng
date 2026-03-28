@echo off
REM Quick test script to verify MTNG application is running

echo ====================================
echo MTNG Application Status Check
echo ====================================
echo.

REM Check if Java process is running
echo Checking for running Java processes...
tasklist /FI "IMAGENAME eq java.exe" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Java process is running
    echo.
) else (
    echo.
    echo ✗ No Java process found. Application may not be running.
    echo.
)

REM Check if port 8443 is listening
echo Checking if HTTPS port 8443 is listening...
netstat -ano | findstr ":8443" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Port 8443 is listening
    echo.
) else (
    echo ✗ Port 8443 is not listening
    echo.
)

echo ====================================
echo Test the application with:
echo https://localhost:8443
echo ====================================
echo.
echo Login credentials:
echo   Admin: admin / admin123
echo   User:  user / user123
echo.
echo Expected behavior after fixes:
echo   1. Login page displays correctly
echo   2. Dashboard loads with meeting options
echo   3. Joining a meeting shows the meeting room interface
echo   4. Participant grid displays without errors
echo   5. Controls panel is fully functional
echo   6. Chat and people sidebars work properly
echo.
echo If you see blank/black page when joining meeting:
echo   1. Open browser console: F12
echo   2. Check for any JavaScript errors
echo   3. Look for "undefined" errors - these should now be fixed
echo.
pause

