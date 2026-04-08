@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo    MTNG APK Builder - Step 2: SDK + Gradle Build
echo ========================================================
echo.

set "FRONTEND=D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"
set "SDK_ROOT=D:\Softwares\android-sdk"
set "CMDLINE_DIR=%SDK_ROOT%\cmdline-tools\latest"

REM === Step 1: Set up Android SDK ===
echo [Step 1] Setting up Android SDK...

if not exist "%SDK_ROOT%" mkdir "%SDK_ROOT%"

if not exist "%CMDLINE_DIR%\bin\sdkmanager.bat" (
    echo   Downloading Android SDK command-line tools...
    powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile '%SDK_ROOT%\cmdline-tools.zip'"

    if not exist "%SDK_ROOT%\cmdline-tools.zip" (
        echo ERROR: Download failed. Check internet connection.
        goto :end
    )

    echo   Extracting...
    powershell -NoProfile -Command "Expand-Archive -Path '%SDK_ROOT%\cmdline-tools.zip' -DestinationPath '%SDK_ROOT%\cmdline-tools-temp' -Force"

    if not exist "%SDK_ROOT%\cmdline-tools" mkdir "%SDK_ROOT%\cmdline-tools"
    if exist "%CMDLINE_DIR%" rmdir /s /q "%CMDLINE_DIR%"
    move "%SDK_ROOT%\cmdline-tools-temp\cmdline-tools" "%CMDLINE_DIR%" >nul 2>&1
    rmdir /s /q "%SDK_ROOT%\cmdline-tools-temp" 2>nul
    del "%SDK_ROOT%\cmdline-tools.zip" 2>nul
    echo   Done!
) else (
    echo   SDK tools already present.
)

set "ANDROID_HOME=%SDK_ROOT%"
set "ANDROID_SDK_ROOT=%SDK_ROOT%"
set "PATH=%CMDLINE_DIR%\bin;%SDK_ROOT%\platform-tools;%PATH%"

echo.

REM === Step 2: Install platform + build-tools ===
echo [Step 2] Installing platform and build-tools...
echo y| call "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >nul 2>&1
echo y| call "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >nul 2>&1
call "%CMDLINE_DIR%\bin\sdkmanager.bat" "platforms;android-34" "build-tools;34.0.0" "platform-tools"
echo.

REM === Step 3: Build APK ===
echo [Step 3] Building APK with Gradle...
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
    echo   SUCCESS! APK created at:
    echo   %APK_DEST%
    echo ========================================================
) else (
    echo.
    echo   ERROR: APK was not created. Check errors above.
)

:end
echo.
pause

