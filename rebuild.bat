@echo off
cd /d D:\IntelliJ Projects Trainings\Mtng
echo Cleaning...
rmdir /s /q target
echo Building with Maven...
call mvn.cmd clean package -DskipTests
echo Build complete
if exist "target\Mtng-0.0.1-SNAPSHOT.jar" (
  echo JAR created successfully
  dir "target\Mtng-0.0.1-SNAPSHOT.jar"
  echo Starting application...
  java -jar "target\Mtng-0.0.1-SNAPSHOT.jar"
) else (
  echo ERROR: JAR not created
  pause
)

