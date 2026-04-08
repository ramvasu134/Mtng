# full-build-apk.ps1
# Self-contained APK build script for MTNG
# Writes all output to D:\IntelliJ Projects Trainings\Mtng\apk_full.log

$ErrorActionPreference = "Continue"
$LOG = "D:\IntelliJ Projects Trainings\Mtng\apk_full.log"
$FRONTEND = "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"
$NODE_DIR = "D:\IntelliJ Projects Trainings\Mtng\target\node"
$NODE = "$NODE_DIR\node.exe"
$NPM  = "$NODE_DIR\npm.cmd"
$NPX  = "$NODE_DIR\npx.cmd"
$SDK_ROOT = "D:\Softwares\android-sdk"
$CMDLINE = "$SDK_ROOT\cmdline-tools\latest"

# CRITICAL: Add node dir to PATH so that npx.cmd can find node.exe internally
$env:PATH = "$NODE_DIR;$env:PATH"

function Log($msg) {
    $ts = Get-Date -Format "HH:mm:ss"
    "$ts $msg" | Tee-Object -FilePath $LOG -Append | Write-Host
}

"" | Out-File $LOG  # clear log
Log "=========================================="
Log "   MTNG APK Full Build Script"
Log "=========================================="

# --- Check Node ---
Log "[1/6] Checking Node.js..."
if (-not (Test-Path $NODE)) {
    Log "ERROR: node.exe not found at $NODE"
    Log "Please run: mvnw.cmd clean package -DskipTests   first"
    exit 1
}
$nodeVer = & $NODE -v 2>&1
Log "  Node: $nodeVer"

# --- Install Capacitor v6 ---
Log "[2/6] Installing Capacitor v6..."
Set-Location $FRONTEND
$result = & $NPM install "@capacitor/core@6" "@capacitor/android@6" 2>&1
Log $result
$result = & $NPM install -D "@capacitor/cli@6" 2>&1
Log $result

# --- Build React for mobile ---
Log "[3/6] Building React app for mobile..."
Set-Location $FRONTEND
if (Test-Path "$FRONTEND\dist\index.html") {
    Log "  dist/ already exists - skipping rebuild"
} else {
    $result = & cmd.exe /c """$NPX"" vite build --config vite.config.mobile.js 2>&1"
    Log $result
    if (-not (Test-Path "$FRONTEND\dist\index.html")) {
        Log "ERROR: React build failed - dist/index.html missing"
        exit 1
    }
}
Log "  React build: OK"

# --- Setup Android project ---
Log "[4/6] Setting up Android project..."
Set-Location $FRONTEND
if (-not (Test-Path "$FRONTEND\android\gradlew.bat")) {
    Log "  Creating Android project..."
    if (Test-Path "$FRONTEND\android") { Remove-Item "$FRONTEND\android" -Recurse -Force }
    # Run cap via cmd so node is properly resolved
    $result = & cmd.exe /c """$NPX"" cap add android 2>&1"
    Log $result
    $result = & cmd.exe /c """$NPX"" cap sync android 2>&1"
    Log $result
} else {
    Log "  Android project already exists - syncing..."
    $result = & cmd.exe /c """$NPX"" cap sync android 2>&1"
    Log $result
}
if (-not (Test-Path "$FRONTEND\android\gradlew.bat")) {
    Log "ERROR: Android project not created"
    exit 1
}
Log "  Android project: OK"

# --- Download Android SDK command-line tools ---
Log "[5/6] Setting up Android SDK..."
if (-not (Test-Path "$CMDLINE\bin\sdkmanager.bat")) {
    Log "  Downloading SDK command-line tools (this may take a few minutes)..."
    New-Item -ItemType Directory -Path "$SDK_ROOT\cmdline-tools" -Force | Out-Null
    $zipUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    $zipOut = "$SDK_ROOT\cmdline-tools.zip"

    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $wc = New-Object System.Net.WebClient
        $wc.DownloadFile($zipUrl, $zipOut)
        Log "  Download complete: $([Math]::Round((Get-Item $zipOut).Length/1MB,1)) MB"
    } catch {
        Log "ERROR: Download failed: $_"
        exit 1
    }

    Log "  Extracting..."
    Expand-Archive -Path $zipOut -DestinationPath "$SDK_ROOT\cmdline-tools-temp" -Force
    $extracted = "$SDK_ROOT\cmdline-tools-temp\cmdline-tools"
    if (Test-Path $extracted) {
        if (Test-Path $CMDLINE) { Remove-Item $CMDLINE -Recurse -Force }
        Move-Item $extracted $CMDLINE
    }
    Remove-Item "$SDK_ROOT\cmdline-tools-temp" -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item $zipOut -Force
    Log "  SDK tools installed"
} else {
    Log "  SDK tools already installed"
}

$env:ANDROID_HOME = $SDK_ROOT
$env:ANDROID_SDK_ROOT = $SDK_ROOT
$env:PATH = "$CMDLINE\bin;$SDK_ROOT\platform-tools;$env:PATH"

Log "  Installing platform-34 and build-tools..."
"y`ny`ny`ny`ny`n" | & "$CMDLINE\bin\sdkmanager.bat" --licenses 2>&1 | Out-Null
$result = & "$CMDLINE\bin\sdkmanager.bat" "platforms;android-34" "build-tools;34.0.0" "platform-tools" 2>&1
Log $result

# --- Build APK ---
Log "[6/6] Building APK with Gradle..."
Set-Location "$FRONTEND\android"
$env:ANDROID_HOME = $SDK_ROOT
$env:ANDROID_SDK_ROOT = $SDK_ROOT

Log "  Running gradlew assembleDebug (first run downloads Gradle ~100MB, be patient)..."
$result = & cmd.exe /c "gradlew.bat assembleDebug 2>&1"
Log $result

$APK_SRC = "$FRONTEND\android\app\build\outputs\apk\debug\app-debug.apk"
$APK_DEST = "D:\IntelliJ Projects Trainings\Mtng\MtngApp.apk"

if (Test-Path $APK_SRC) {
    Copy-Item $APK_SRC $APK_DEST -Force
    Log ""
    Log "=========================================="
    Log "  SUCCESS! APK created:"
    Log "  $APK_DEST"
    Log "  Size: $([Math]::Round((Get-Item $APK_DEST).Length/1MB,2)) MB"
    Log "=========================================="
} else {
    Log "FAILED: APK not found at expected path"
    Log "Check log above for Gradle errors"
}

