@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo    StudyProxy-Java  Windows 版
echo ============================================
echo.

rem ---------- 1. 检查配置文件 ----------
if not exist "config.yaml" (
    echo [错误] 当前目录缺少 config.yaml
    echo        请复制 config.example.yaml 为 config.yaml 并填写学号/密码。
    echo.
    pause
    exit /b 1
)

rem ---------- 2. 检查 Java ----------
set "JAVA_OK="
where java >nul 2>nul && set "JAVA_OK=1"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_OK=1"
if not defined JAVA_OK (
    echo [错误] 未检测到 Java 运行环境。
    echo        请安装 JRE 8 ~ 21（推荐 JDK 17），并确保 java 在 PATH 中。
    echo        下载地址: https://adoptium.net/temurin/releases/
    echo.
    pause
    exit /b 1
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JVER=%%v"
echo [OK] Java 已就绪 %JVER%

rem ---------- 3. 检查 Python 与 OCR 依赖 ----------
set "PY_OK="
where python >nul 2>nul && set "PY_OK=1"
if not defined PY_OK (
    echo [提示] 未检测到 Python，将跳过本地 OCR 服务。
    echo        验证码识别需要 Python 3.8+：https://www.python.org/downloads/
    echo.
    start "StudyProxy" "StudyProxy.exe"
    goto :eof
)

python -c "import flask, ddddocr, requests" >nul 2>nul
if errorlevel 1 (
    echo [提示] 缺少 OCR 依赖，正在安装 flask / ddddocr / requests ...
    python -m pip install -r requirements.txt
    if errorlevel 1 (
        echo [警告] 依赖安装失败，可手动执行: python -m pip install -r requirements.txt
    )
)

rem ---------- 4. 启动 OCR 服务 ----------
echo [1/2] 启动 OCR 验证码识别服务（端口 8083）...
start "StudyProxy OCR" /min cmd /c "python ocr_service.py"
timeout /t 3 /nobreak >nul

rem ---------- 5. 启动主程序 ----------
echo [2/2] 启动主程序 StudyProxy.exe ...
start "StudyProxy" "StudyProxy.exe"

echo.
echo 启动完成。如主程序窗口一闪而过，请检查 config.yaml 与 logs 目录下的日志。
echo.
pause
