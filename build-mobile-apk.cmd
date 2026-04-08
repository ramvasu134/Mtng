@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo    MTNG Mobile App - Complete APK Builder
echo ========================================================
echo.

REM === Use the project's own Node.js from target/node ===
set "NODE_DIR=D:\IntelliJ Projects Trainings\Mtng\target\node"
set "PATH=%NODE_DIR%;%NODE_DIR%\node_modules\.bin;%PATH%"
set "FRONTEND=D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"

cd /d "%FRONTEND%"

echo [1/7] Checking Node.js...
call "%NODE_DIR%\node.exe" -v
if errorlevel 1 (
    echo ERROR: Node.js not found at %NODE_DIR%
    echo Please run Maven build first: mvnw.cmd clean package -DskipTests
    pause
    exit /b 1
)
echo   Node: OK
echo.

echo [2/7] Installing Capacitor packages...
call "%NODE_DIR%\npm.cmd" install @capacitor/core @capacitor/android
call "%NODE_DIR%\npm.cmd" install -D @capacitor/cli
echo.

echo [3/7] Building React app for mobile (dist folder)...
call "%NODE_DIR%\npx.cmd" vite build --config vite.config.mobile.js
if not exist "%FRONTEND%\dist\index.html" (
    echo ERROR: React build failed!
    pause
    exit /b 1
)
echo   React build: OK
echo.

echo [4/7] Setting up Android SDK...
set "SDK_ROOT=D:\Softwares\android-sdk"
set "CMDLINE_DIR=%SDK_ROOT%\cmdline-tools\latest"

if not exist "%SDK_ROOT%" mkdir "%SDK_ROOT%"

if not exist "%CMDLINE_DIR%\bin\sdkmanager.bat" (
    echo   Downloading Android SDK command-line tools...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile '%SDK_ROOT%\cmdline-tools.zip'"

    if not exist "%SDK_ROOT%\cmdline-tools.zip" (
        echo ERROR: Failed to download SDK tools. Check internet.
        pause
        exit /b 1
    )

    echo   Extracting...
    powershell -NoProfile -Command "Expand-Archive -Path '%SDK_ROOT%\cmdline-tools.zip' -DestinationPath '%SDK_ROOT%\cmdline-tools-temp' -Force"

    if not exist "%SDK_ROOT%\cmdline-tools" mkdir "%SDK_ROOT%\cmdline-tools"
    if exist "%CMDLINE_DIR%" rmdir /s /q "%CMDLINE_DIR%"
    move "%SDK_ROOT%\cmdline-tools-temp\cmdline-tools" "%CMDLINE_DIR%" >nul
    rmdir /s /q "%SDK_ROOT%\cmdline-tools-temp" 2>nul
    del "%SDK_ROOT%\cmdline-tools.zip" 2>nul
    echo   SDK tools extracted!
) else (
    echo   SDK tools already installed.
)

set "ANDROID_HOME=%SDK_ROOT%"
set "ANDROID_SDK_ROOT=%SDK_ROOT%"
set "PATH=%CMDLINE_DIR%\bin;%SDK_ROOT%\platform-tools;%PATH%"
echo.

echo [5/7] Installing Android build tools...
echo y| call "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >nul 2>&1
echo y| call "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >nul 2>&1
call "%CMDLINE_DIR%\bin\sdkmanager.bat" "platforms;android-34" "build-tools;34.0.0" "platform-tools"
echo.

echo [6/7] Creating Android project via Capacitor...
cd /d "%FRONTEND%"
if exist android rmdir /s /q android
call "%NODE_DIR%\npx.cmd" cap add android
call "%NODE_DIR%\npx.cmd" cap sync android

if not exist "%FRONTEND%\android\gradlew.bat" (
    echo ERROR: Android project creation failed!
    pause
    exit /b 1
)

REM Patch AndroidManifest.xml with required permissions
set "MANIFEST=%FRONTEND%\android\app\src\main\AndroidManifest.xml"
powershell -NoProfile -Command ^
  "$f = Get-Content '%MANIFEST%' -Raw;" ^
  "if ($f -notmatch 'RECORD_AUDIO') {" ^
  "  $perms = '    <uses-permission android:name=\"android.permission.INTERNET\" />' + [Environment]::NewLine;" ^
  "  $perms += '    <uses-permission android:name=\"android.permission.RECORD_AUDIO\" />' + [Environment]::NewLine;" ^
  "  $perms += '    <uses-permission android:name=\"android.permission.MODIFY_AUDIO_SETTINGS\" />' + [Environment]::NewLine;" ^
  "  $perms += '    <uses-permission android:name=\"android.permission.CAMERA\" />' + [Environment]::NewLine;" ^
  "  $perms += '    <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />' + [Environment]::NewLine;" ^
  "  $perms += '    <uses-permission android:name=\"android.permission.WAKE_LOCK\" />' + [Environment]::NewLine;" ^
  "  $f = $f -replace '(<application)', ($perms + '    $1');" ^
  "  Set-Content '%MANIFEST%' $f -Encoding UTF8;" ^
  "  Write-Host '  Permissions patched!';" ^
  "} else { Write-Host '  Permissions already present.'; }"
echo.

echo [7/7] Building APK with Gradle...
cd /d "%FRONTEND%\android"
set "ANDROID_HOME=%SDK_ROOT%"
set "ANDROID_SDK_ROOT=%SDK_ROOT%"
call gradlew.bat assembleDebug

set "APK_SRC=%FRONTEND%\android\app\build\outputs\apk\debug\app-debug.apk"
set "APK_DEST=D:\IntelliJ Projects Trainings\Mtng\MtngApp.apk"

if exist "%APK_SRC%" (
    copy /y "%APK_SRC%" "%APK_DEST%" >nul
    echo.
    echo ========================================================
    echo.
    echo   SUCCESS! Your APK installer is ready!
    echo.
    echo   File: %APK_DEST%
    echo.
    echo   Transfer this .apk to your Android phone and install.
    echo   Enable "Install from unknown sources" in settings.
    echo.
    echo ========================================================
) else (
    echo.
    echo   APK build failed. Check errors above.
)

echo.
pause

