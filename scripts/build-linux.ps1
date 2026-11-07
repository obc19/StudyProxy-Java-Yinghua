# ==========================================================================
#  StudyProxy-Java - 构建 Linux 发布包（Windows 侧执行）
#
#  产物：dist\StudyProxy-Java-linux\ 与 dist\StudyProxy-Java-linux.tar.gz
#  内容：主程序 JAR + config.yaml + start.sh + ocr_service.py
#        + requirements.txt + README.md（不含源码 / 测试 / Windows EXE）
#
#  用法： .\build-linux.ps1           仅构建
#         .\build-linux.ps1 -Clean    先 mvn clean 再构建
#
#  说明：打包细节全部由 scripts\make_tar_release.py 负责，
#        本脚本只做「环境检查 + Maven 构建 + 调用打包器」。
# ==========================================================================
param(
    [switch]$Clean
)

$ErrorActionPreference = "Stop"
# 本脚本位于 scripts\，仓库根目录是上一级
$Root = Split-Path $PSScriptRoot -Parent
Set-Location $Root

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "[成功] $msg" -ForegroundColor Green }
function Write-Err($msg)  { Write-Host "[错误] $msg" -ForegroundColor Red }

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Err "未找到 mvn，请先安装 Maven 3.6+ 并加入 PATH"
    exit 1
}

Write-Step "构建可执行 JAR"
if ($Clean) {
    & mvn -B clean
    if ($LASTEXITCODE -ne 0) { Write-Err "mvn clean 失败"; exit 1 }
}
& mvn -B "-Dmaven.test.skip=true" package
if ($LASTEXITCODE -ne 0) { Write-Err "Maven 构建失败"; exit 1 }

$Jar = Get-ChildItem -Path (Join-Path $Root "target") -Filter "StudyProxy-Java-*.jar" -ErrorAction SilentlyContinue |
       Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1
if (-not $Jar) { Write-Err "未生成 target\StudyProxy-Java-*.jar"; exit 1 }
Write-Ok $Jar.FullName

Write-Step "组装并打包 Linux 发布包"
$py = (Get-Command python -ErrorAction SilentlyContinue)
if (-not $py) { $py = (Get-Command py -ErrorAction SilentlyContinue) }
if (-not $py) {
    Write-Err "未找到 python，无法生成 tar.gz（也可手动压缩 dist\StudyProxy-Java-linux）"
    exit 1
}
& $py.Source "scripts\make_tar_release.py"
if ($LASTEXITCODE -ne 0) { Write-Err "打包失败"; exit 1 }

Write-Host ""
Write-Ok "构建完成"
Write-Host "  部署到 Linux："
Write-Host "    tar -xzf StudyProxy-Java-linux.tar.gz"
Write-Host "    cd StudyProxy-Java-linux && chmod +x start.sh && ./start.sh"
Write-Host ""
