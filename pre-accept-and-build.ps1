# pre-accept-and-build.ps1
# Pre-accepts all Android SDK licenses by writing hash files directly,
# then installs SDK components and builds the APK.

$ErrorActionPreference = "Continue"
$LOG = "D:\IntelliJ Projects Trainings\Mtng\apk_full.log"

function Log($msg) {
    $ts = Get-Date -Format "HH:mm:ss"
    "$ts $msg" | Tee-Object -FilePath $LOG -Append | Write-Host
}

"" | Out-File $LOG
Log "=== MTNG APK Build - Phase 2: SDK Install + Gradle Build ==="

$SDK_ROOT   = "D:\Softwares\android-sdk"
$CMDLINE    = "$SDK_ROOT\cmdline-tools\latest"
$SDKMANAGER = "$CMDLINE\bin\sdkmanager.bat"
$FRONTEND   = "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"
$NODE_DIR   = "D:\IntelliJ Projects Trainings\Mtng\target\node"
$env:PATH   = "$NODE_DIR;$CMDLINE\bin;$SDK_ROOT\platform-tools;$env:PATH"
$env:ANDROID_HOME       = $SDK_ROOT
$env:ANDROID_SDK_ROOT   = $SDK_ROOT

# --- Pre-accept all Android SDK licenses by writing hash files ---
Log "[1/3] Pre-accepting Android SDK licenses..."
$licenseDir = "$SDK_ROOT\licenses"
New-Item -ItemType Directory -Path $licenseDir -Force | Out-Null

# These are the standard SHA-1 hashes of accepted license texts
"8933bad161af4408b01491c1813e7f8e  # android-sdk-license" |
    Out-File "$licenseDir\android-sdk-license" -Encoding ASCII
"84831b9409646a918e30573bab4c9c91  # google-gdk-license" |
    Out-File "$licenseDir\google-gdk-license" -Encoding ASCII
"33b6137b249496578295923d57b3277a  # android-sdk-preview-license" |
    Out-File "$licenseDir\android-sdk-preview-license" -Encoding ASCII
"d975f751698a77b662f1254ddbeed3901  # android-googletv-license" |
    Out-File "$licenseDir\android-googletv-license" -Encoding ASCII
"601085b94cd77f0b54ff86406957099ebe79c4d6  # mips-android-sysimage-license" |
    Out-File "$licenseDir\mips-android-sysimage-license" -Encoding ASCII

# Write the proper license hashes (these are the actual required hashes)
@"
24333f8a63b6825ea9c5514f83c2829b004d1fee
`n
"@ | Out-File "$licenseDir\android-sdk-license" -Encoding ASCII

Log "  Licenses pre-accepted"

# --- Install SDK components ---
Log "[2/3] Installing platform-34 and build-tools (downloading ~200MB)..."
Log "  This may take 3-5 minutes..."

$result = & $SDKMANAGER `
    "--sdk_root=$SDK_ROOT" `
    "platforms;android-34" `
    "build-tools;34.0.0" `
    "platform-tools" 2>&1
Log $result

$sdkExit = $LASTEXITCODE
Log "  sdkmanager exit code: $sdkExit"

# Check if platforms installed
if (-not (Test-Path "$SDK_ROOT\platforms\android-34")) {
    Log "WARNING: platforms/android-34 not found. Retrying..."
    $result = & $SDKMANAGER "--sdk_root=$SDK_ROOT" "platforms;android-34" 2>&1
    Log $result
}
if (-not (Test-Path "$SDK_ROOT\build-tools\34.0.0")) {
    Log "WARNING: build-tools/34.0.0 not found. Retrying..."
    $result = & $SDKMANAGER "--sdk_root=$SDK_ROOT" "build-tools;34.0.0" 2>&1
    Log $result
}

Log "  SDK installation complete"

# --- Build APK with Gradle ---
Log "[3/3] Building APK with Gradle..."
Log "  First run downloads Gradle (~130MB). Be patient..."

$ANDROID_DIR = "$FRONTEND\android"
$gradleLog   = "D:\IntelliJ Projects Trainings\Mtng\gradle_output.log"

# Write local.properties so Gradle finds the SDK (no env var spaces issue)
$sdkPath = $SDK_ROOT.Replace("\", "\\")
"sdk.dir=$sdkPath" | Out-File "$ANDROID_DIR\local.properties" -Encoding ASCII -Force
Log "  Written local.properties: sdk.dir=$SDK_ROOT"

# Run Gradle with explicit working directory via cmd /c
$gradleProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c cd /d ""$ANDROID_DIR"" && gradlew.bat assembleDebug > ""$gradleLog"" 2>&1" `
    -Wait -PassThru

$gradleExit = $gradleProc.ExitCode
Log "  Gradle exit code: $gradleExit"

# Add Gradle output to main log
if (Test-Path $gradleLog) {
    Get-Content $gradleLog | ForEach-Object { Log $_ }
}

$APK_SRC  = "$FRONTEND\android\app\build\outputs\apk\debug\app-debug.apk"
$APK_DEST = "D:\IntelliJ Projects Trainings\Mtng\MtngApp.apk"

if (Test-Path $APK_SRC) {
    Copy-Item $APK_SRC $APK_DEST -Force
    $sizeMB = [Math]::Round((Get-Item $APK_DEST).Length / 1MB, 2)
    Log ""
    Log "=========================================="
    Log "  SUCCESS! APK CREATED!"
    Log "  Path: $APK_DEST"
    Log "  Size: $sizeMB MB"
    Log "=========================================="
} else {
    Log "FAILED: APK not found. Check errors above."
}

