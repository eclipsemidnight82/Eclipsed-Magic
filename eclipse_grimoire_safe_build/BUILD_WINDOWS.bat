@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title The Eclipse Grimoire - Forge build

echo ========================================================
echo The Eclipse Grimoire - Minecraft 1.20.1 Forge build
echo ========================================================
echo.
echo This script uses Java, Windows curl/tar, and Forge Gradle.
echo It downloads the official Forge MDK from maven.minecraftforge.net.
echo It does NOT change Windows security settings.
echo.

where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java was not found. Install Java JDK 17 before building.
  goto :failed
)
java -version
if errorlevel 1 goto :failed

echo.
echo IMPORTANT: The JDK must be Java 17 for Minecraft 1.20.1 Forge.
echo.
set "FORGE_VERSION=1.20.1-47.4.0"
set "WORKSPACE=%~dp0_forge_mdk_workspace"
set "ZIPFILE=%~dp0forge-mdk-download.zip"
set "FORGE_URL=https://maven.minecraftforge.net/net/minecraftforge/forge/%FORGE_VERSION%/forge-%FORGE_VERSION%-mdk.zip"

if not exist "%WORKSPACE%\gradlew.bat" (
  echo Downloading Forge MDK from the official Forge Maven server...
  where curl.exe >nul 2>&1
  if errorlevel 1 (
    echo ERROR: curl.exe was not found. Windows 10 or 11 is required.
    goto :failed
  )
  curl.exe --fail --location --retry 2 "%FORGE_URL%" --output "%ZIPFILE%"
  if errorlevel 1 goto :failed
  if not exist "%WORKSPACE%" mkdir "%WORKSPACE%"
  tar.exe -xf "%ZIPFILE%" -C "%WORKSPACE%"
  if errorlevel 1 goto :failed
)
if not exist "%WORKSPACE%\gradlew.bat" (
  echo ERROR: The Forge MDK Gradle wrapper was not found.
  goto :failed
)

rem Only replace the src directory inside this project's own Forge workspace.
if exist "%WORKSPACE%\src" rmdir /s /q "%WORKSPACE%\src"
xcopy "%~dp0src" "%WORKSPACE%\src\" /E /I /Q /Y >nul
if errorlevel 1 goto :failed
copy /Y "%~dp0build.gradle" "%WORKSPACE%\build.gradle" >nul
if errorlevel 1 goto :failed
copy /Y "%~dp0settings.gradle" "%WORKSPACE%\settings.gradle" >nul
if errorlevel 1 goto :failed
copy /Y "%~dp0gradle.properties" "%WORKSPACE%\gradle.properties" >nul
if errorlevel 1 goto :failed

pushd "%WORKSPACE%"
call gradlew.bat build
set "RESULT=%ERRORLEVEL%"
popd
if not "%RESULT%"=="0" goto :failed

set "BUILT_JAR=%WORKSPACE%\build\libs\eclipse_grimoire-0.1.0-alpha.jar"
if not exist "%BUILT_JAR%" (
  echo ERROR: Build finished but expected JAR is missing.
  goto :failed
)
if not exist "%~dp0dist" mkdir "%~dp0dist"
copy /Y "%BUILT_JAR%" "%~dp0dist\eclipse_grimoire-0.1.0-alpha.jar" >nul
if errorlevel 1 goto :failed

echo.
echo SUCCESS: dist\eclipse_grimoire-0.1.0-alpha.jar
echo This is still an UNTESTED alpha: test in a backup world before publishing.
pause
exit /b 0

:failed
echo.
echo BUILD FAILED. Read the error above; do not upload the source ZIP as a mod.
pause
exit /b 1
