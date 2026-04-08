@echo off
setlocal enabledelayedexpansion

set "LOG=D:\IntelliJ Projects Trainings\Mtng\apk_build.log"
set "FRONTEND=D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"
set "SDK_ROOT=D:\Softwares\android-sdk"
set "CMDLINE_DIR=%SDK_ROOT%\cmdline-tools\latest"

echo START > "%LOG%"
echo [Step 1] Setting up Android SDK... >> "%LOG%"

if not exist "%SDK_ROOT%" mkdir "%SDK_ROOT%"

if not exist "%CMDLINE_DIR%\bin\sdkmanager.bat" (
    echo   Downloading SDK tools... >> "%LOG%"
    powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile '%SDK_ROOT%\cmdline-tools.zip'" >> "%LOG%" 2>&1

    if not exist "%SDK_ROOT%\cmdline-tools.zip" (
        echo ERROR: Download failed >> "%LOG%"
        goto :end
    )

    echo   Extracting... >> "%LOG%"
    powershell -NoProfile -Command "Expand-Archive -Path '%SDK_ROOT%\cmdline-tools.zip' -DestinationPath '%SDK_ROOT%\cmdline-tools-temp' -Force" >> "%LOG%" 2>&1

    if not exist "%SDK_ROOT%\cmdline-tools" mkdir "%SDK_ROOT%\cmdline-tools"
    if exist "%CMDLINE_DIR%" rmdir /s /q "%CMDLINE_DIR%"
    move "%SDK_ROOT%\cmdline-tools-temp\cmdline-tools" "%CMDLINE_DIR%" >> "%LOG%" 2>&1
    rmdir /s /q "%SDK_ROOT%\cmdline-tools-temp" 2>nul
    del "%SDK_ROOT%\cmdline-tools.zip" 2>nul
    echo   SDK tools installed >> "%LOG%"
) else (
    echo   SDK tools already present >> "%LOG%"
)

set "ANDROID_HOME=%SDK_ROOT%"
set "ANDROID_SDK_ROOT=%SDK_ROOT%"
set "PATH=%CMDLINE_DIR%\bin;%SDK_ROOT%\platform-tools;%PATH%"

echo [Step 2] Installing platform and build-tools... >> "%LOG%"
echo y| call "%CMDLINE_DIR%\bin\sdkmanager.bat" --licenses >> "%LOG%" 2>&1
call "%CMDLINE_DIR%\bin\sdkmanager.bat" "platforms;android-34" "build-tools;34.0.0" "platform-tools" >> "%LOG%" 2>&1
echo   SDK install exit code: %ERRORLEVEL% >> "%LOG%"

echo [Step 3] Building APK... >> "%LOG%"
cd /d "%FRONTEND%\android"

call gradlew.bat assembleDebug >> "%LOG%" 2>&1
echo   Gradle exit code: %ERRORLEVEL% >> "%LOG%"

set "APK_SRC=%FRONTEND%\android\app\build\outputs\apk\debug\app-debug.apk"
set "APK_DEST=D:\IntelliJ Projects Trainings\Mtng\MtngApp.apk"

if exist "%APK_SRC%" (
    copy /y "%APK_SRC%" "%APK_DEST%" >> "%LOG%" 2>&1
    echo SUCCESS: APK at %APK_DEST% >> "%LOG%"
) else (
    echo FAILED: No APK generated >> "%LOG%"
)

:end
echo BUILD_SCRIPT_DONE >> "%LOG%"

