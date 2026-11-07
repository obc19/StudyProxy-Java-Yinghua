@echo off
chcp 65001 >nul

rem ========================================
rem 英华学堂自动学习工具 - 一键启动脚本
rem ========================================

setlocal enabledelayedexpansion

rem 检查Java环境
echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装Java 8或更高版本
    pause
    exit /b 1
)

echo Java环境检查通过

rem 检查Python环境（OCR服务需要）
echo 检查Python环境...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: 未找到Python环境，OCR验证码识别功能将不可用
    set OCR_AVAILABLE=0
) else (
    set OCR_AVAILABLE=1
)

echo 开始启动服务...
echo ========================================

rem 启动OCR服务（如果Python可用）
if %OCR_AVAILABLE% equ 1 (
    echo 启动OCR验证码识别服务...
    start "OCR Service" /min cmd /c "python ocr_service.py"
    timeout /t 3 >nul
    echo OCR服务启动完成
) else (
    echo OCR服务未启动（Python不可用）
)

echo 启动主程序...
start "StudyProxy" "StudyProxy.exe"

echo ========================================
echo 服务启动完成！
echo ========================================
echo.
echo 使用说明:
echo 1. 主程序已在后台启动
if %OCR_AVAILABLE% equ 1 (
echo 2. OCR服务已在后台启动（用于验证码识别）
echo 3. 如需停止服务，请在任务管理器中结束进程
echo    - 主程序: StudyProxy.exe
echo    - OCR服务: python.exe
) else (
echo 2. OCR服务未启动，验证码识别功能不可用
echo 3. 如需停止服务，请在任务管理器中结束 StudyProxy.exe 进程
)
echo.
echo 日志文件: app.log
echo 配置文件: config.yaml
echo.

rem 等待用户确认
pause
