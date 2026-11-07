#!/usr/bin/env bash
# ==========================================================================
#  StudyProxy-Java — Linux 打包脚本
#
#  产物：dist/StudyProxy-Java-linux/ 与同名 tar.gz
#  内容：主程序 JAR + config.yaml + start.sh + ocr_service.py
#        + requirements.txt + README.md
#        （不含源码、测试、Windows EXE、Docker 相关文件）
#
#  用法：
#    ./build-linux.sh          构建并打包
#    ./build-linux.sh --clean  先 mvn clean 再构建
# ==========================================================================
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

VERSION="1.0.0"
JAR_NAME="StudyProxy-Java-${VERSION}.jar"
PKG_NAME="StudyProxy-Java-linux"
DIST_DIR="dist/${PKG_NAME}"
SRC_DIR="release-linux"

CLEAN=0
if [ "${1:-}" = "--clean" ]; then
    CLEAN=1
fi

title() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }
ok()    { printf '\033[32m[成功]\033[0m %s\n' "$*"; }
err()   { printf '\033[31m[错误]\033[0m %s\n' "$*" >&2; }

command -v mvn  >/dev/null 2>&1 || { err "未找到 mvn，请先安装 Maven 3.6+"; exit 1; }
command -v java >/dev/null 2>&1 || { err "未找到 java，请先安装 JDK 8+"; exit 1; }

title "构建可执行 JAR"
if [ "$CLEAN" -eq 1 ]; then
    mvn -B clean
fi
mvn -B -Dmaven.test.skip=true package

[ -f "target/${JAR_NAME}" ] || { err "构建失败：未生成 target/${JAR_NAME}"; exit 1; }

title "组装发布目录"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/logs"

cp "target/${JAR_NAME}"        "$DIST_DIR/"
cp "${SRC_DIR}/start.sh"       "$DIST_DIR/"
cp "${SRC_DIR}/ocr_service.py" "$DIST_DIR/"
cp "${SRC_DIR}/requirements.txt" "$DIST_DIR/"
cp "${SRC_DIR}/README.md"      "$DIST_DIR/"

# 配置文件：优先使用项目根目录下已填好的 config.yaml，否则用模板
if [ -f "config.yaml" ]; then
    cp "config.yaml" "$DIST_DIR/config.yaml"
else
    cp "${SRC_DIR}/config.yaml" "$DIST_DIR/config.yaml"
fi

chmod +x "$DIST_DIR/start.sh"
# 统一换行符，避免 Linux 上出现 "/bin/bash^M: bad interpreter"
sed -i 's/\r$//' "$DIST_DIR/start.sh" "$DIST_DIR/ocr_service.py" "$DIST_DIR/config.yaml"

title "生成压缩包"
rm -f "dist/${PKG_NAME}.tar.gz"
( cd dist && tar -czf "${PKG_NAME}.tar.gz" "${PKG_NAME}" )

echo
ok "构建完成"
echo "  目录: $(pwd)/${DIST_DIR}"
echo "  压缩: $(pwd)/dist/${PKG_NAME}.tar.gz"
echo
echo "  部署到目标机器："
echo "    tar -xzf ${PKG_NAME}.tar.gz"
echo "    cd ${PKG_NAME} && ./start.sh"
