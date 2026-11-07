#!/usr/bin/env bash
# ==========================================================================
#  StudyProxy-Java — Linux 打包脚本
#
#  产物：dist/StudyProxy-Java-linux/ 与 dist/StudyProxy-Java-linux.tar.gz
#  内容：主程序 JAR + config.yaml + start.sh + ocr_service.py
#        + requirements.txt + README.md（不含源码 / 测试 / Windows EXE）
#
#  用法：
#    ./scripts/build-linux.sh            构建并打包
#    ./scripts/build-linux.sh --clean    先 mvn clean 再构建
#
#  说明：打包细节全部由 scripts/make_tar_release.py 负责，
#        本脚本只做「环境检查 + Maven 构建 + 调用打包器」。
# ==========================================================================
set -euo pipefail

# 本脚本位于 scripts/，切到仓库根目录
cd "$(dirname "${BASH_SOURCE[0]}")/.."

title() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }
ok()    { printf '\033[32m[成功]\033[0m %s\n' "$*"; }
err()   { printf '\033[31m[错误]\033[0m %s\n' "$*" >&2; }

command -v mvn  >/dev/null 2>&1 || { err "未找到 mvn，请先安装 Maven 3.6+"; exit 1; }
command -v java >/dev/null 2>&1 || { err "未找到 java，请先安装 JDK 8+"; exit 1; }

if command -v python3 >/dev/null 2>&1; then
    PY=python3
elif command -v python >/dev/null 2>&1; then
    PY=python
else
    err "未找到 python3，无法生成 tar.gz"
    exit 1
fi

title "构建可执行 JAR"
if [ "${1:-}" = "--clean" ]; then
    mvn -B clean
fi
mvn -B -Dmaven.test.skip=true package

title "组装并打包 Linux 发布包"
"$PY" scripts/make_tar_release.py

echo
ok "构建完成"
echo "  部署到目标机器："
echo "    tar -xzf StudyProxy-Java-linux.tar.gz"
echo "    cd StudyProxy-Java-linux && chmod +x start.sh && ./start.sh"
echo
