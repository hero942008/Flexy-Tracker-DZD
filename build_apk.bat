@echo off
setlocal enabledelayedexpansion
title Build Flexy APK

echo ========================================================
echo       Building Flexy APK (Android Debug Build)
echo ========================================================
echo.

rem 1. Check or create .env file
if not exist .env (
    if exist .env.example (
        copy .env.example .env >nul
        echo [INFO] Created .env file from .env.example
    ) else (
        type nul > .env
        echo [INFO] Created empty .env file
    )
)

rem 2. Auto-detect or prompt for Android SDK
if "%ANDROID_HOME%"=="" (
    if "%ANDROID_SDK_ROOT%"=="" (
        if not exist local.properties (
            set "FOUND_SDK="
            if exist "%LOCALAPPDATA%\Android\Sdk" set "FOUND_SDK=%LOCALAPPDATA%\Android\Sdk"
            if exist "%USERPROFILE%\AppData\Local\Android\Sdk" set "FOUND_SDK=%USERPROFILE%\AppData\Local\Android\Sdk"
            if exist "C:\Android\sdk" set "FOUND_SDK=C:\Android\sdk"
            if exist "D:\Android\sdk" set "FOUND_SDK=D:\Android\sdk"

            if defined FOUND_SDK (
                echo [INFO] Found Android SDK at: !FOUND_SDK!
                set "ESCAPED_SDK=!FOUND_SDK:\=\\!"
                echo sdk.dir=!ESCAPED_SDK! > local.properties
                echo [INFO] Created local.properties with detected Android SDK.
            ) else (
                echo.
                echo [WARNING] Android SDK not found automatically!
                echo Please make sure Android Studio / Android SDK is installed on your computer.
                echo If installed in a custom location, enter the path below.
                echo Example: C:\Users\YourUser\AppData\Local\Android\Sdk
                echo.
                set /p USER_SDK="Enter Android SDK Path (or press Enter to skip): "
                if defined USER_SDK (
                    set "ESCAPED_SDK=!USER_SDK:\=\\!"
                    echo sdk.dir=!ESCAPED_SDK! > local.properties
                    echo [INFO] Created local.properties with your SDK path.
                )
            )
        )
    )
)

rem 3. Ensure gradle-wrapper.jar exists if missing
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [INFO] gradle-wrapper.jar not found locally.
    echo [INFO] Downloading Gradle Wrapper JAR...
    if not exist "gradle\wrapper" mkdir "gradle\wrapper"
    powershell -Command "[Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar', 'gradle\wrapper\gradle-wrapper.jar')" 2>nul
)

rem 4. Execute build with fallback to system gradle if needed
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [INFO] Running Gradle Wrapper...
    call gradlew.bat assembleDebug
) else (
    echo [WARN] Gradle wrapper jar not found. Attempting system gradle...
    gradle assembleDebug
)

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo   BUILD SUCCESSFUL!
    echo   APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo   BUILD FAILED!
    echo   If the error is 'SDK location not found':
    echo   Make sure Android SDK is installed via Android Studio,
    echo   or specify sdk.dir in local.properties.
    echo ========================================================
)

echo.
pause
