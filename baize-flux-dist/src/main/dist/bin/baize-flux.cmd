@echo off
setlocal EnableExtensions

rem ============================================================
rem 在前台运行一个本地 Baize Flux 批处理任务
rem ============================================================

rem 当前脚本所在目录，例如 D:\baize-flux\bin\
set "SCRIPT_DIR=%~dp0"

rem 默认将脚本上一级目录作为 BAIZE_FLUX_HOME
if not defined BAIZE_FLUX_HOME (
    for %%I in ("%SCRIPT_DIR%..") do set "BAIZE_FLUX_HOME=%%~fI"
)

rem 配置目录
if not defined BAIZE_FLUX_CONF_DIR (
    set "BAIZE_FLUX_CONF_DIR=%BAIZE_FLUX_HOME%\config"
)

rem 日志目录
if not defined BAIZE_FLUX_LOG_DIR (
    set "BAIZE_FLUX_LOG_DIR=%BAIZE_FLUX_HOME%\logs"
)

rem 优先使用 JAVA_HOME
if defined JAVA_HOME (
    set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"

    if not exist "%JAVA_BIN%" (
        echo [ERROR] JAVA_HOME is invalid: %JAVA_HOME% 1>&2
        echo [ERROR] Cannot find: %JAVA_BIN% 1>&2
        exit /b 1
    )
) else (
    where java >nul 2>nul

    if errorlevel 1 (
        echo [ERROR] Java 8 or later is required. 1>&2
        echo [ERROR] Please set JAVA_HOME or add java to PATH. 1>&2
        exit /b 1
    )

    set "JAVA_BIN=java"
)

rem 创建日志目录
if not exist "%BAIZE_FLUX_LOG_DIR%" (
    mkdir "%BAIZE_FLUX_LOG_DIR%"

    if errorlevel 1 (
        echo [ERROR] Failed to create log directory: %BAIZE_FLUX_LOG_DIR% 1>&2
        exit /b 1
    )
)

rem 默认配置文件
if "%~1"=="" (
    set "LAUNCHER_ARGS=--config "%BAIZE_FLUX_CONF_DIR%\baize-flux.yaml""
) else (
    set "LAUNCHER_ARGS=%*"
)

rem 默认 JVM 参数
set "JAVA_OPTS=-Xms256m -Xmx1024m"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+ExitOnOutOfMemoryError"
set "JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8"
set "JAVA_OPTS=%JAVA_OPTS% "-Dlog4j.configurationFile=%BAIZE_FLUX_CONF_DIR%\log4j2.xml""
set "JAVA_OPTS=%JAVA_OPTS% "-Dbaize.flux.log.dir=%BAIZE_FLUX_LOG_DIR%""

rem 添加用户自定义 JVM 参数
if defined BAIZE_FLUX_JAVA_OPTS (
    set "JAVA_OPTS=%JAVA_OPTS% %BAIZE_FLUX_JAVA_OPTS%"
)

echo [INFO] BAIZE_FLUX_HOME=%BAIZE_FLUX_HOME%
echo [INFO] JAVA_BIN=%JAVA_BIN%
echo [INFO] Starting Baize Flux...

"%JAVA_BIN%" %JAVA_OPTS% ^
    -cp "%BAIZE_FLUX_HOME%\lib\*" ^
    com.baize.flux.launcher.LocalSyncLauncher ^
    %LAUNCHER_ARGS%

set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo [ERROR] Baize Flux exited with code %EXIT_CODE%. 1>&2
)

exit /b %EXIT_CODE%