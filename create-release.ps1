# 英华学堂自动学习工具 - 发布包制作脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "英华学堂自动学习工具 - 发布包制作" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$releaseDir = "release"

# 创建发布目录
Write-Host "[1/4] 创建发布目录..." -ForegroundColor Yellow
if (Test-Path $releaseDir) {
    Remove-Item -Path $releaseDir -Recurse -Force
}
New-Item -ItemType Directory -Path $releaseDir | Out-Null

# 复制主程序文件
Write-Host "[2/4] 复制主程序文件..." -ForegroundColor Yellow
Copy-Item "target\StudyProxy.exe" $releaseDir
Copy-Item "config.yaml" $releaseDir
Copy-Item "ocr_service.py" $releaseDir
Copy-Item "README_WINDOWS.md" "$releaseDir\README.md"

# 创建启动脚本
Write-Host "[3/4] 创建启动脚本..." -ForegroundColor Yellow
$startScript = @"
@echo off
chcp 65001 >nul
echo 启动英华学堂自动学习工具...
echo.
start "" "StudyProxy.exe"
"@
$startScript | Out-File -FilePath "$releaseDir\启动程序.bat" -Encoding ASCII

# 创建OCR启动脚本
Write-Host "[4/4] 创建OCR启动脚本..." -ForegroundColor Yellow
$ocrScript = @"
@echo off
chcp 65001 >nul
echo 启动OCR验证码识别服务...
echo.
python ocr_service.py
pause
"@
$ocrScript | Out-File -FilePath "$releaseDir\启动OCR服务.bat" -Encoding ASCII

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "发布包制作完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "发布目录: $releaseDir\" -ForegroundColor Cyan
Write-Host ""
Write-Host "包含文件:" -ForegroundColor Cyan
Get-ChildItem $releaseDir | ForEach-Object { Write-Host "  - $($_.Name)" }
Write-Host ""
Write-Host "使用说明:" -ForegroundColor Yellow
Write-Host "  1. 将整个 release 文件夹复制到目标机器"
Write-Host "  2. 确保已安装 Java 8 或更高版本"
Write-Host "  3. 修改 config.yaml 配置文件"
Write-Host "  4. 双击'启动程序.bat'运行"
Write-Host ""
