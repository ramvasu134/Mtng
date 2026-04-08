@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo    MTNG Mobile App - APK Builder
echo    This script sets up everything and builds your APK
echo ========================================================
echo.

REM === Step 0: Check prerequisites ===
echo [Step 0] Checking prerequisites...
where node >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js not found. Please install Node.js first.
    pause
    exit /b 1
)
echo   Node.js: OK
where npm >nul 2>&1
if errorlevel 1 (
    echo ERROR: npm not found. Please install Node.js first.
    pause
    exit /b 1
)
echo   npm: OK

REM Check for Java
where java >nul 2>&1
if errorlevel 1 (
    echo WARNING: Java not found in PATH. Gradle may fail.
) else (
    echo   Java: OK
)

echo.

REM === Step 1: Set up Android SDK ===
echo [Step 1] Setting up Android SDK command-line tools...

set "SDK_ROOT=D:\Softwares\android-sdk"
set "CMDLINE_DIR=%SDK_ROOT%\cmdline-tools\latest"

if not exist "%SDK_ROOT%" mkdir "%SDK_ROOT%"

REM Download cmdline tools if not present
if not exist "%CMDLINE_DIR%\bin\sdkmanager.bat" (
    echo   Downloading Android SDK command-line tools...

    REM Use PowerShell to download
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile '%SDK_ROOT%\cmdline-tools.zip'"

    if not exist "%SDK_ROOT%\cmdline-tools.zip" (
        echo   ERROR: Failed to download command-line tools
        pause
        exit /b 1
    )

    echo   Extracting command-line tools...
    powershell -NoProfile -Command "Expand-Archive -Path '%SDK_ROOT%\cmdline-tools.zip' -DestinationPath '%SDK_ROOT%\cmdline-tools-temp' -Force"

    if not exist "%SDK_ROOT%\cmdline-tools" mkdir "%SDK_ROOT%\cmdline-tools"

    REM The zip extracts to cmdline-tools/ folder, we need it under latest/
    if exist "%SDK_ROOT%\cmdline-tools-temp\cmdline-tools" (
        if exist "%CMDLINE_DIR%" rmdir /s /q "%CMDLINE_DIR%"
        move "%SDK_ROOT%\cmdline-tools-temp\cmdline-tools" "%CMDLINE_DIR%" >nul
        rmdir /s /q "%SDK_ROOT%\cmdline-tools-temp" 2>nul
    )

    del "%SDK_ROOT%\cmdline-tools.zip" 2>nul
    echo   SDK command-line tools installed!
) else (
    echo   SDK command-line tools already installed.
)

REM Set environment
set "ANDROID_HOME=%SDK_ROOT%"
set "ANDROID_SDK_ROOT=%SDK_ROOT%"
set "PATH=%CMDLINE_DIR%\bin;%SDK_ROOT%\platform-tools;%PATH%"

echo   ANDROID_HOME=%ANDROID_HOME%
echo.

REM === Step 2: Install required SDK components ===
echo [Step 2] Installing Android platform and build tools...

REM Accept licenses non-interactively
echo y| "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >nul 2>&1
echo y| "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >nul 2>&1

REM Install required components
call "%CMDLINE_DIR%\bin\sdkmanager.bat" "platforms;android-34" "build-tools;34.0.0" "platform-tools"
echo   SDK components installed!
echo.

REM === Step 3: Navigate to frontend and install Capacitor ===
echo [Step 3] Installing Capacitor in frontend project...
cd /d "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"

REM Install capacitor packages
call npm install @capacitor/core@latest @capacitor/android@latest
call npm install -D @capacitor/cli@latest

echo   Capacitor packages installed!
echo.

REM === Step 4: Build the React app for mobile ===
echo [Step 4] Building React app for mobile (into dist/ folder)...
call npx vite build --config vite.config.mobile.js

if not exist "dist\index.html" (
    echo ERROR: Build failed - dist/index.html not found
    pause
    exit /b 1
)
echo   React build complete!
echo.

REM === Step 5: Add Android platform ===
echo [Step 5] Adding Android platform via Capacitor...

REM Remove old android folder if corrupt
if exist "android" rmdir /s /q "android"

call npx cap add android
call npx cap sync android

if not exist "android\gradlew.bat" (
    echo ERROR: Android project was not created properly
    pause
    exit /b 1
)
echo   Android project created!
echo.

REM === Step 6: Patch Android permissions for WebRTC ===
echo [Step 6] Patching Android manifest for WebRTC permissions...
set "MANIFEST=android\app\src\main\AndroidManifest.xml"

REM Use PowerShell to inject permissions
powershell -NoProfile -Command ^
    "$f = Get-Content '%MANIFEST%' -Raw; " ^
    "if ($f -notmatch 'RECORD_AUDIO') { " ^
    "  $perms = @'`n" ^
    "    <uses-permission android:name=\"android.permission.INTERNET\" />`n" ^
    "    <uses-permission android:name=\"android.permission.RECORD_AUDIO\" />`n" ^
    "    <uses-permission android:name=\"android.permission.MODIFY_AUDIO_SETTINGS\" />`n" ^
    "    <uses-permission android:name=\"android.permission.CAMERA\" />`n" ^
    "    <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />`n" ^
    "    <uses-permission android:name=\"android.permission.WAKE_LOCK\" />`n" ^
    "'@; " ^
    "  $f = $f -replace '(<application)', ($perms + '`n    $1'); " ^
    "  Set-Content '%MANIFEST%' $f -Encoding UTF8; " ^
    "  Write-Host '  Permissions added!'; " ^
    "} else { Write-Host '  Permissions already present.'; }"

echo.

REM === Step 7: Build the APK ===
echo [Step 7] Building APK with Gradle (this may take a few minutes)...
cd /d "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend\android"

set "ANDROID_HOME=%SDK_ROOT%"
set "ANDROID_SDK_ROOT=%SDK_ROOT%"

call gradlew.bat assembleDebug

set "APK_SRC=app\build\outputs\apk\debug\app-debug.apk"
set "APK_DEST=D:\IntelliJ Projects Trainings\Mtng\MtngApp.apk"

if exist "%APK_SRC%" (
    copy /y "%APK_SRC%" "%APK_DEST%" >nul
    echo.
    echo ========================================================
    echo   SUCCESS! Your APK installer has been created!
    echo.
    echo   Location: %APK_DEST%
    echo.
    echo   Transfer this file to your Android phone and install it.
    echo   You may need to enable "Install from unknown sources"
    echo   in your phone settings.
    echo ========================================================
) else (
    echo.
    echo ERROR: APK was not generated. Check the Gradle output above.
    echo If you see SDK-related errors, try running this script again.
)

echo.
pause

