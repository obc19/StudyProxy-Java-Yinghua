@echo off
chcp 65001 >nul
setlocal
rem 本脚本位于 scripts\，先切到仓库根目录
cd /d "%~dp0.."

echo ========================================
echo  构建 Windows 可执行包
echo ========================================
echo.

echo [1/2] Maven 构建（生成 target\StudyProxy.exe 与 JAR）...
call mvn -B clean package -DskipTests
if errorlevel 1 (
    echo.
    echo [错误] Maven 构建失败
    pause
    exit /b 1
)
if not exist "target\StudyProxy.exe" (
    echo [错误] 未生成 target\StudyProxy.exe，请确认 pom.xml 的 windows-exe profile 已启用
    pause
    exit /b 1
)

echo.
echo [2/2] 组装 dist\StudyProxy-Java-windows.zip ...
where python >nul 2>nul
if errorlevel 1 (
    echo [提示] 未找到 python，跳过压缩。可稍后手动执行:
    echo        python scripts\make_zip_release.py
    pause
    exit /b 0
)
python "scripts\make_zip_release.py"

echo.
echo 产物:
echo   target\StudyProxy.exe
echo   target\StudyProxy-Java-1.0.0.jar
echo   dist\StudyProxy-Java-windows.zip
echo.
pause
