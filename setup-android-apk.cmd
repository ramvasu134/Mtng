@echo off
echo Setting up Android configuration for your Mtng Web App...
echo =========================================================

cd src\main\frontend

echo Installing Capacitor core...
call npm install @capacitor/core @capacitor/android

echo Installing Capacitor CLI...
call npm install -D @capacitor/cli

echo Initializing Capacitor...
call npx cap init MtngApp com.mtng.app --web-dir dist

echo Syncing with React build...
call npm run build
call npx cap add android
call npx cap sync android

echo.
echo ==============================================================
echo Android project successfully configured!
echo The Android source code is now in src\main\frontend\android
echo.
echo To generate the actual .apk installer file, you need Android Studio.
echo Do the following:
echo 1. Run:  cd src\main\frontend ^&^& npx cap open android
echo 2. Android Studio will open your new mobile project.
echo 3. Wait for it to sync Gradle.
echo 4. Click on the top menu: Build -] Build Bundle(s) / APK(s) -] Build APK(s)
echo 5. A popup will appear in the bottom right corner with the path to your .apk file.
echo.
echo Note: Ensure your development server is running and accessible over your local network by mobile context, or build the frontend with the correct production URL.
echo ==============================================================
pause

