@echo off
set "NODE_DIR=D:\IntelliJ Projects Trainings\Mtng\target\node"
set "PATH=%NODE_DIR%;%NODE_DIR%\node_modules\.bin;%PATH%"

cd /d "D:\IntelliJ Projects Trainings\Mtng\src\main\frontend"

echo [1/4] Installing Capacitor packages (v6 for Node 20 compat)... > build_mobile.log
call "%NODE_DIR%\npm.cmd" install @capacitor/core@6 @capacitor/android@6 >> build_mobile.log 2>&1
echo EXIT_CODE_1=%ERRORLEVEL% >> build_mobile.log

call "%NODE_DIR%\npm.cmd" install -D @capacitor/cli@6 >> build_mobile.log 2>&1
echo EXIT_CODE_2=%ERRORLEVEL% >> build_mobile.log

echo [2/4] Building React for mobile... >> build_mobile.log
call "%NODE_DIR%\npx.cmd" vite build --config vite.config.mobile.js >> build_mobile.log 2>&1
echo EXIT_CODE_3=%ERRORLEVEL% >> build_mobile.log

echo [3/4] Adding Android platform... >> build_mobile.log
if exist android rmdir /s /q android
call "%NODE_DIR%\npx.cmd" cap add android >> build_mobile.log 2>&1
echo EXIT_CODE_4=%ERRORLEVEL% >> build_mobile.log

call "%NODE_DIR%\npx.cmd" cap sync android >> build_mobile.log 2>&1
echo EXIT_CODE_5=%ERRORLEVEL% >> build_mobile.log

echo [4/4] Setup complete >> build_mobile.log
echo SETUP_DONE >> build_mobile.log
