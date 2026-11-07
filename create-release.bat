@echo off
chcp 65001 >nul
echo ========================================
echo 英华学堂自动学习工具 - 发布包制作
echo ========================================
echo.

if exist release rmdir /s /q release
mkdir release

echo [1/3] 复制主程序文件...
copy target\StudyProxy.exe release\ >nul
copy config.yaml release\ >nul
copy ocr_service.py release\ >nul
copy README_WINDOWS.md release\README.md >nul

echo [2/3] 创建启动脚本...
echo @echo off > release\启动程序.bat
echo chcp 65001 ^>nul >> release\启动程序.bat
echo echo 启动英华学堂自动学习工具... >> release\启动程序.bat
echo start "" "StudyProxy.exe" >> release\启动程序.bat

echo [3/3] 创建OCR启动脚本...
echo @echo off > release\启动OCR服务.bat
echo chcp 65001 ^>nul >> release\启动OCR服务.bat
echo echo 启动OCR验证码识别服务... >> release\启动OCR服务.bat
echo python ocr_service.py >> release\启动OCR服务.bat
echo pause >> release\启动OCR服务.bat

echo.
echo ========================================
echo 发布包制作完成！
echo ========================================
echo.
echo 发布目录: release\
echo.
dir /b release
echo.
pause
