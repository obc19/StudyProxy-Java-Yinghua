@echo off
chcp 65001 >nul
echo ========================================
echo 英华学堂自动学习工具 - Windows打包脚本
echo ========================================
echo.

echo [1/3] 清理旧的构建文件...
call mvn clean

echo.
echo [2/3] 编译项目并打包成EXE...
call mvn package -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo [错误] 打包失败！
    pause
    exit /b 1
)

echo.
echo [3/3] 检查生成的文件...
if exist "target\StudyProxy.exe" (
    echo [成功] EXE文件已生成: target\StudyProxy.exe
    
    echo.
    echo ========================================
    echo 打包完成！
    echo ========================================
    echo.
    echo 生成的文件:
    echo   - target\StudyProxy.exe (Windows可执行文件)
    echo   - target\StudyProxy-Java-1.0.0.jar (Java JAR文件)
    echo.
    echo 使用说明:
    echo   1. 将 StudyProxy.exe 和 config.yaml 放在同一目录
    echo   2. 确保系统已安装 Java 8 或更高版本
    echo   3. 双击运行 StudyProxy.exe
    echo.
) else (
    echo [错误] EXE文件生成失败！
    pause
    exit /b 1
)

pause
