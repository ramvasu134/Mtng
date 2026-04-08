# check-state.ps1 - Runs in a separate process, writes results to a file
$out = "D:\IntelliJ Projects Trainings\Mtng\state_check.txt"

"=== STATE CHECK ===" | Out-File $out
"Date: $(Get-Date)" | Out-File $out -Append

# Check platform-tools
"--- platform-tools ---" | Out-File $out -Append
$pt = "D:\Softwares\platform-tools-latest-windows\platform-tools"
if (Test-Path $pt) {
    Get-ChildItem $pt | Select-Object Name | Out-File $out -Append
} else {
    "NOT FOUND: $pt" | Out-File $out -Append
}

# Check android-sdk
"--- android-sdk ---" | Out-File $out -Append
$sdk = "D:\Softwares\android-sdk"
if (Test-Path $sdk) {
    Get-ChildItem $sdk -Recurse | Select-Object FullName, Length | Out-File $out -Append
} else {
    "NOT FOUND: $sdk" | Out-File $out -Append
}

# Check java
"--- java ---" | Out-File $out -Append
try { (Get-Command java -ErrorAction Stop).Source | Out-File $out -Append }
catch { "java not in PATH" | Out-File $out -Append }

# Check node
"--- node ---" | Out-File $out -Append
$nodeDir = "D:\IntelliJ Projects Trainings\Mtng\target\node"
if (Test-Path "$nodeDir\node.exe") {
    & "$nodeDir\node.exe" -v 2>&1 | Out-File $out -Append
} else {
    "node not found at $nodeDir" | Out-File $out -Append
}

# Check if android project exists
"--- android project ---" | Out-File $out -Append
$ap = "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend\android"
if (Test-Path "$ap\gradlew.bat") {
    "gradlew.bat EXISTS" | Out-File $out -Append
} else {
    "gradlew.bat MISSING" | Out-File $out -Append
}

# Check if APK already built
"--- APK ---" | Out-File $out -Append
$apk = "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend\android\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apk) {
    "APK EXISTS: $apk" | Out-File $out -Append
} else {
    "APK NOT YET BUILT" | Out-File $out -Append
}

# Check build log
"--- apk_build.log ---" | Out-File $out -Append
$buildLog = "D:\IntelliJ Projects Trainings\Mtng\apk_build.log"
if (Test-Path $buildLog) {
    Get-Content $buildLog | Out-File $out -Append
} else {
    "No build log found" | Out-File $out -Append
}

"=== DONE ===" | Out-File $out -Append

