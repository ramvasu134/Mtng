@echo off
REM Test Script for 3 Fixed Issues - MTNG Meeting Platform

echo.
echo ======================================================
echo    MTNG Meeting Platform - 3 Issues Fixed Test
echo ======================================================
echo.
echo This script will help you test the 3 fixed issues:
echo   1. Participant names showing as "Guest"
echo   2. No audio heard from other participants
echo   3. Recording not saving automatically or manually
echo.

:menu
echo.
echo Select what you want to do:
echo   1. Start the application
echo   2. View application logs
echo   3. Check if application is running
echo   4. Kill running Java process
echo   5. View test instructions
echo   6. Exit
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" goto start_app
if "%choice%"=="2" goto view_logs
if "%choice%"=="3" goto check_app
if "%choice%"=="4" goto kill_app
if "%choice%"=="5" goto test_instructions
if "%choice%"=="6" goto end

echo Invalid choice!
goto menu

:start_app
echo.
echo Starting MTNG application...
cd /d D:\IntelliJ Projects Trainings\Mtng
java -jar target\Mtng-0.0.1-SNAPSHOT.jar
goto menu

:view_logs
echo.
echo Recent application logs:
echo.
tail -50 app-startup.log
echo.
pause
goto menu

:check_app
echo.
echo Checking if application is running on port 8443...
netstat -ano | findstr ":8443"
if %errorlevel% equ 0 (
    echo.
    echo ✓ Application is running!
    echo Access it at: https://localhost:8443
) else (
    echo.
    echo ✗ Application is not running
)
echo.
pause
goto menu

:kill_app
echo.
echo Killing Java process...
taskkill /IM java.exe /F
echo Done!
echo.
pause
goto menu

:test_instructions
echo.
echo ======================================================
echo   TESTING THE 3 FIXED ISSUES
echo ======================================================
echo.
echo BEFORE YOU START:
echo   1. Make sure application is running on https://localhost:8443
echo   2. Have 2 browsers/tabs ready (or use incognito)
echo   3. Login credentials:
echo      - Admin: admin / admin123
echo      - User: user / user123
echo.
echo.
echo TEST 1: Participant Names (Should NOT be "Guest")
echo ─────────────────────────────────────────────────
echo Steps:
echo   1. Login as ADMIN (admin/admin123)
echo   2. Create a meeting (title: "Name Test")
echo   3. Click "Join Meeting"
echo   4. In another browser, login as USER (user/user123)
echo   5. Join the same meeting
echo.
echo EXPECTED RESULTS:
echo   ✓ Your name shown in participant grid (not "Guest")
echo   ✓ Admin shown as "admin" or admin's display name
echo   ✓ User shown as "user" or user's display name
echo   ✓ Each has an avatar with first initial letter
echo.
echo IF FAILING:
echo   • Check browser F12 Developer Tools > Console
echo   • Look for "displayName" in signal messages
echo   • Verify participant name is stored in participantNames ref
echo.
echo.
echo TEST 2: Audio Communication (Should hear each other)
echo ──────────────────────────────────────────────────
echo Steps:
echo   1. Both admin and user joined to meeting (from Test 1)
echo   2. Check microphone is enabled (green 🎤 on your tile)
echo   3. Admin speaks into microphone
echo   4. User should hear admin's voice
echo   5. User speaks, admin should hear user's voice
echo.
echo EXPECTED RESULTS:
echo   ✓ Green indicator (🎤) on participant speaking
echo   ✓ Can hear other participants clearly
echo   ✓ Audio quality is good
echo   ✓ No lag or delay
echo.
echo IF NOT HEARING AUDIO:
echo   • Check browser F12 Developer Tools > Console
echo   • Look for these messages:
echo     - "🔊 ontrack event received from [username]"
echo     - "✓ Setting audio srcObject for [username]"
echo     - "Connection state: connected"
echo     - "ICE connection state: connected"
echo   • Check microphone permissions in browser
echo   • Try "Join as Listener" to test receive-only
echo   • Check if your browser's audio is muted globally
echo.
echo.
echo TEST 3: Recording (Should save to file)
echo ──────────────────────────────────────
echo Steps:
echo   1. In the meeting (both participants present)
echo   2. Click RED "Record" button (bottom control panel)
echo   3. Let recording run for at least 10 seconds
echo   4. Speak during recording
echo   5. Click RED "Stop Rec" button
echo.
echo EXPECTED RESULTS:
echo   ✓ Download dialog appears (.webm file)
echo   ✓ File saved to Downloads folder
echo   ✓ File size > 0 bytes
echo   ✓ File can be played in any media player
echo   ✓ Console shows "✓ Recording uploaded successfully"
echo   ✓ Recording saved to server database
echo.
echo IF RECORDING DOESN'T SAVE:
echo   • Check browser F12 Developer Tools > Console
echo   • Look for:
echo     - "Recording stopped. Blob size: [SIZE]"
echo     - "Saving recording to server..."
echo     - "✓ Recording uploaded successfully"
echo   • If "Blob size: 0" → recording had no data
echo   • Check Network tab for failed POST to /api/recordings
echo   • Verify browser allows file download
echo   • Check server logs in app-startup.log
echo.
echo.
echo DEBUGGING IN BROWSER CONSOLE (F12):
echo ──────────────────────────────────
echo These messages indicate successful operations:
echo   ✓ "Adding 1 local tracks to peer [username]"
echo   ✓ "Connection state for [username]: connected"
echo   ✓ "ICE connection state for [username]: connected"
echo   ✓ "🔊 ontrack event received"
echo   ✓ "✓ Setting audio srcObject"
echo   ✓ "Recording started with mime type"
echo   ✓ "✓ Recording uploaded successfully"
echo.
echo.
echo SUMMARY:
echo ────────
echo Issue 1 - Fixed ✓ Names preserved in participantNames ref
echo Issue 2 - Fixed ✓ Enhanced audio logging and error handling
echo Issue 3 - Fixed ✓ Improved recording save with proper DOM methods
echo.
echo.
pause
goto menu

:end
echo.
echo Thank you for testing MTNG!
echo If issues persist, check the documentation files:
echo   - THREE_ISSUES_FIXED.md (detailed technical info)
echo   - RENDERING_FIX_GUIDE.md (rendering error fixes)
echo   - QUICK_FIX_REFERENCE.md (quick reference)
echo.
pause

