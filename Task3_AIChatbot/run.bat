@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo       RAJ AI - Desktop Java Assistant Launcher
echo ========================================================
echo.

if not exist "target\Task3_AIChatbot-1.0.0-jar-with-dependencies.jar" (
    echo [*] Building project package...
    call mvn clean package -DskipTests
)

if exist "target\Task3_AIChatbot-1.0.0-jar-with-dependencies.jar" (
    echo [*] Starting RAJ AI...
    start javaw -jar "target\Task3_AIChatbot-1.0.0-jar-with-dependencies.jar"
) else (
    echo [ERROR] JAR file not found. Please ensure JDK 17+ and Maven are installed.
    pause
)
