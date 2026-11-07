# ==========================================================================
#  StudyProxy-Java - 构建 Linux 发布包（Windows 侧执行）
#
#  产物：dist\StudyProxy-Java-linux\ 与 dist\StudyProxy-Java-linux.tar.gz
#  内容：主程序 JAR + config.yaml + start.sh + ocr_service.py
#        + requirements.txt + README.md
#        （不含源码、测试、Windows EXE、Docker 文件）
#
#  用法： .\build-linux.ps1
#         .\build-linux.ps1 -Clean     先 mvn clean 再构建
# ==========================================================================
param(
    [switch]$Clean
)

$ErrorActionPreference = "Stop"
$Root = $PSScriptRoot
Set-Location $Root

$Version  = "1.0.0"
$JarName  = "StudyProxy-Java-$Version.jar"
$PkgName  = "StudyProxy-Java-linux"
$DistRoot = Join-Path $Root "dist"
$DistDir  = Join-Path $DistRoot $PkgName
$SrcDir   = Join-Path $Root "release-linux"

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "[成功] $msg" -ForegroundColor Green }
function Write-Err($msg)  { Write-Host "[错误] $msg" -ForegroundColor Red }

# ---------------------------------------------------------------- 前置检查
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Err "未找到 mvn，请先安装 Maven 3.6+ 并加入 PATH"
    exit 1
}

# ---------------------------------------------------------------- 构建
Write-Step "构建可执行 JAR"
if ($Clean) {
    & mvn -B clean
    if ($LASTEXITCODE -ne 0) { Write-Err "mvn clean 失败"; exit 1 }
}
& mvn -B "-Dmaven.test.skip=true" package
if ($LASTEXITCODE -ne 0) { Write-Err "Maven 构建失败"; exit 1 }

$JarPath = Join-Path $Root "target\$JarName"
if (-not (Test-Path $JarPath)) { Write-Err "未生成 $JarPath"; exit 1 }

# ---------------------------------------------------------------- 组装
Write-Step "组装发布目录"
if (Test-Path $DistRoot) { Remove-Item $DistRoot -Recurse -Force }
New-Item -ItemType Directory -Path (Join-Path $DistDir "logs") -Force | Out-Null

Copy-Item $JarPath                                $DistDir
Copy-Item (Join-Path $SrcDir "start.sh")          $DistDir
Copy-Item (Join-Path $SrcDir "ocr_service.py")    $DistDir
Copy-Item (Join-Path $SrcDir "requirements.txt")  $DistDir
Copy-Item (Join-Path $SrcDir "README.md")         $DistDir

# 配置文件：优先用根目录已填好的 config.yaml，否则用模板
$ConfigSrc = Join-Path $Root "config.yaml"
if (-not (Test-Path $ConfigSrc)) { $ConfigSrc = Join-Path $SrcDir "config.yaml" }
Copy-Item $ConfigSrc (Join-Path $DistDir "config.yaml")

# Linux 上必须是 LF，否则会报 bad interpreter
Write-Step "规范化换行符为 LF"
foreach ($name in @("start.sh", "ocr_service.py", "config.yaml", "README.md", "requirements.txt")) {
    $p = Join-Path $DistDir $name
    if (-not (Test-Path $p)) { continue }
    $text = [System.IO.File]::ReadAllText($p)
    $text = $text -replace "`r`n", "`n"
    [System.IO.File]::WriteAllText($p, $text, (New-Object System.Text.UTF8Encoding($false)))
}

# ---------------------------------------------------------------- 打包
Write-Step "生成 tar.gz（start.sh 权限 755）"
$py = Get-Command python -ErrorAction SilentlyContinue
if ($py) {
    & python (Join-Path $SrcDir "make_tar.py")
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[警告] tar.gz 生成失败，可手动打包 dist\$PkgName 目录" -ForegroundColor Yellow
    }
} else {
    Write-Host "[警告] 未找到 python，跳过 tar.gz；请把 dist\$PkgName 整个目录上传到 Linux 后自行打包" -ForegroundColor Yellow
}

# ---------------------------------------------------------------- 结果
Write-Host ""
Write-Ok "构建完成"
Write-Host "  目录: $DistDir"
$tgz = Join-Path $DistRoot "$PkgName.tar.gz"
if (Test-Path $tgz) { Write-Host "  压缩: $tgz" }
Write-Host ""
Write-Host "  部署到 Linux:"
Write-Host "    tar -xzf $PkgName.tar.gz"
Write-Host "    cd $PkgName && ./start.sh"
Write-Host ""
