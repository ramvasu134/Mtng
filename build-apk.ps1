# build-apk.ps1
# This script will automatically set up the Android SDK and compile your APK!

$ErrorActionPreference = "Stop"

$SdkPath = "D:\Softwares\android-sdk"
$CmdLineToolsZip = "$SdkPath\cmdline-tools.zip"
$CmdLineToolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

Write-Host "====================================================="
Write-Host "   Automated MTNG APK Builder (No Android Studio) "
Write-Host "====================================================="
Write-Host ""

# 1. Setup Android SDK folder structure
if (-not (Test-Path $SdkPath)) {
    New-Item -ItemType Directory -Path $SdkPath | Out-Null
    Write-Host "Created Android SDK directory at $SdkPath"
}

# 2. Download and Extract Command Line Tools
$LatestContentDir = "$SdkPath\cmdline-tools\latest"
if (-not (Test-Path "$LatestContentDir\bin\sdkmanager.bat")) {
    Write-Host "Downloading Android SDK Command Line Tools..."
    Invoke-WebRequest -Uri $CmdLineToolsUrl -OutFile $CmdLineToolsZip

    Write-Host "Extracting Command Line Tools..."
    Expand-Archive -Path $CmdLineToolsZip -DestinationPath "$SdkPath\cmdline-tools" -Force

    # Rename 'cmdline-tools' inside 'cmdline-tools' to 'latest'
    Rename-Item "$SdkPath\cmdline-tools\cmdline-tools" "$SdkPath\cmdline-tools\latest"
    Remove-Item $CmdLineToolsZip -Force
}

# 3. Set Environment Variables for this session
$env:ANDROID_HOME = $SdkPath
$env:PATH += ";$SdkPath\cmdline-tools\latest\bin"
Write-Host "ANDROID_HOME is set to $env:ANDROID_HOME"

# 4. Accept Licenses and install required platform & build-tools
Write-Host "Accepting Android SDK licenses and installing build tools..."
cmd.exe /c "yes | sdkmanager --licenses > NUL"
cmd.exe /c "sdkmanager `"platforms;android-34`" `"build-tools;34.0.0`""

# 5. Build React app and generate Capacitor code
Write-Host "Re-building React App and Syncing to Android..."
Set-Location "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"
npm run build
npx cap sync android

# 6. Build the APK using Gradle wrapper inside the Capacitor android folder
Write-Host "Compiling the APK using Gradle! This may take a few minutes..."
Set-Location "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend\android"
$env:JAVA_HOME="" # Ensure gradle picks up system Java or path
cmd.exe /c "gradlew.bat assembleDebug"

# 7. Collect the output
$ApkPath = "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend\android\app\build\outputs\apk\debug\app-debug.apk"
$FinalDest = "D:\IntelliJ Projects Trainings\Mtng\MtngApp-Installer.apk"

if (Test-Path $ApkPath) {
    Copy-Item $ApkPath $FinalDest -Force
    Write-Host ""
    Write-Host "====================================================="
    Write-Host " SUCCESS! "
    Write-Host " Your APK has been created and saved to: "
    Write-Host " $FinalDest "
    Write-Host "====================================================="
} else {
    Write-Host "Failed to build the APK. Please check the Gradle output above." -ForegroundColor Red
}

Read-Host -Prompt "Press Enter to exit"

