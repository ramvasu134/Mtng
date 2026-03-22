@echo off
echo ============================================
echo   MTNG - Meeting Management Platform
echo ============================================
echo.

cd /d "D:\IntelliJ Projects Trainings\Mtng"

echo [1] Killing existing Java processes...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 5 /nobreak >nul
echo     Done.
echo.

echo [2] Building and Starting MTNG Application on port 8080...
echo.
echo     App URL:      http://localhost:8080
echo     Login:        admin/admin123 (ADMIN) or user/user123 (USER)
echo     H2 Console:   http://localhost:8080/h2-console
echo     H2 JDBC URL:  jdbc:h2:mem:mtngdb
echo     H2 User:      sa
echo     H2 Password:  (leave empty)
echo.
echo ============================================
echo.
call mvnw.cmd spring-boot:run
