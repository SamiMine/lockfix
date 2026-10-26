@echo off
set DIR=%~dp0
set APP_HOME=%DIR%
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
    echo Gradle wrapper JAR not found. Please run: gradle wrapper --gradle-version 8.9
    exit /b 1
)

if "%JAVA_HOME%"=="" (
    set JAVA_CMD=java
) else (
    set JAVA_CMD=%JAVA_HOME%\bin\java
)

"%JAVA_CMD%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
