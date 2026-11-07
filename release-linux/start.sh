#!/usr/bin/env bash
# ==========================================================================
#  StudyProxy-Java Linux 启动脚本
#
#  首次运行会自动检测并安装缺失的依赖：
#    - Java 运行时（JRE 8+，用于运行主程序）
#    - Python 3 + pip（用于验证码识别服务）
#    - OCR 依赖：flask / ddddocr / requests
#
#  用法：
#    ./start.sh               正常启动（先起 OCR 服务，再跑主程序）
#    ./start.sh --no-ocr      不启动 OCR 服务（已有远程/外部 OCR 时使用）
#    ./start.sh --no-install  不自动安装依赖，仅做检测，缺失则报错退出
#    ./start.sh --check       只检测环境，不启动程序
# ==========================================================================
set -uo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$APP_DIR" || { echo "[错误] 无法进入目录: $APP_DIR"; exit 1; }

APP_NAME="StudyProxy-Java"
APP_JAR="StudyProxy-Java-1.0.0.jar"
CONFIG_FILE="config.yaml"
OCR_SCRIPT="ocr_service.py"
REQ_FILE="requirements.txt"
OCR_PORT="8083"
JAVA_MIN_MAJOR=8

START_OCR=1
AUTO_INSTALL=1
CHECK_ONLY=0
OCR_PID=""
# 传给主程序的额外参数（放在 -- 之后）
APP_ARGS=()

# ---------------------------------------------------------------- 输出工具
TERM_COLORS="$(tput colors 2>/dev/null || echo 0)"
case "$TERM_COLORS" in ''|*[!0-9]*) TERM_COLORS=0 ;; esac
if [ -t 1 ] && [ "$TERM_COLORS" -ge 8 ]; then
    C_RED=$'\033[31m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'
    C_CYAN=$'\033[36m'; C_BOLD=$'\033[1m'; C_RESET=$'\033[0m'
else
    C_RED=""; C_GREEN=""; C_YELLOW=""; C_CYAN=""; C_BOLD=""; C_RESET=""
fi

info()  { printf '%s[信息]%s %s\n' "$C_CYAN"   "$C_RESET" "$*"; }
ok()    { printf '%s[成功]%s %s\n' "$C_GREEN"  "$C_RESET" "$*"; }
warn()  { printf '%s[警告]%s %s\n' "$C_YELLOW" "$C_RESET" "$*"; }
err()   { printf '%s[错误]%s %s\n' "$C_RED"    "$C_RESET" "$*" >&2; }
title() { printf '\n%s%s==> %s%s\n' "$C_BOLD" "$C_CYAN" "$*" "$C_RESET"; }

# ---------------------------------------------------------------- 参数解析
while [ $# -gt 0 ]; do
    case "$1" in
        --no-ocr)     START_OCR=0 ;;
        --no-install) AUTO_INSTALL=0 ;;
        --check)      CHECK_ONLY=1 ;;
        -h|--help)
            sed -n '2,15p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        --)           shift; APP_ARGS=("$@"); break ;;
        *)
            err "未知参数: $1 （使用 --help 查看用法；传给主程序的参数请放在 -- 之后）"
            exit 2 ;;
    esac
    shift
done

# ---------------------------------------------------------------- 基础工具
SUDO=""
if [ "$(id -u)" -ne 0 ]; then
    if command -v sudo >/dev/null 2>&1; then
        SUDO="sudo"
    fi
fi

has_cmd() { command -v "$1" >/dev/null 2>&1; }

# 判断系统包管理器
detect_pkg_mgr() {
    if has_cmd apt-get;   then echo "apt";    return; fi
    if has_cmd dnf;       then echo "dnf";    return; fi
    if has_cmd yum;       then echo "yum";    return; fi
    if has_cmd apk;       then echo "apk";    return; fi
    if has_cmd pacman;    then echo "pacman"; return; fi
    if has_cmd zypper;    then echo "zypper"; return; fi
    echo "unknown"
}

# 用系统包管理器安装软件包（可能包含多个包名）
pkg_install() {
    local mgr; mgr="$(detect_pkg_mgr)"
    case "$mgr" in
        apt)
            $SUDO apt-get update -y -q || true
            DEBIAN_FRONTEND=noninteractive $SUDO apt-get install -y -q "$@" ;;
        dnf)    $SUDO dnf install -y "$@" ;;
        yum)    $SUDO yum install -y "$@" ;;
        apk)    $SUDO apk add --no-cache "$@" ;;
        pacman) $SUDO pacman -Sy --noconfirm "$@" ;;
        zypper) $SUDO zypper --non-interactive install -y "$@" ;;
        *)
            err "未识别的包管理器，请手动安装: $*"
            return 1 ;;
    esac
}

# ---------------------------------------------------------------- Java 检测
# 输出 java 主版本号（1.8 -> 8，17.0.9 -> 17），失败返回空
java_major_of() {
    local bin="$1" out
    out="$("$bin" -version 2>&1 | head -n 1)"
    # 形如: openjdk version "17.0.9" / java version "1.8.0_392"
    local ver
    ver="$(printf '%s' "$out" | sed -n 's/.*version "\([^"]*\)".*/\1/p')"
    [ -z "$ver" ] && return 1
    case "$ver" in
        1.*) printf '%s\n' "$ver" | cut -d. -f2 ;;
        *)   printf '%s\n' "$ver" | cut -d. -f1 | tr -dc '0-9' ;;
    esac
}

find_java() {
    # 1) 优先 JAVA_HOME
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        local mj; mj="$(java_major_of "$JAVA_HOME/bin/java" || true)"
        if [ -n "$mj" ] && [ "$mj" -ge "$JAVA_MIN_MAJOR" ]; then
            echo "$JAVA_HOME/bin/java"; return 0
        fi
    fi
    # 2) PATH
    if has_cmd java; then
        local mj; mj="$(java_major_of "$(command -v java)" || true)"
        if [ -n "$mj" ] && [ "$mj" -ge "$JAVA_MIN_MAJOR" ]; then
            echo "$(command -v java)"; return 0
        fi
    fi
    # 3) 常见安装位置
    local cand
    for cand in /usr/bin/java /usr/local/bin/java /opt/java/bin/java \
                /usr/lib/jvm/*/bin/java /opt/jdk*/bin/java; do
        [ -x "$cand" ] || continue
        local mj; mj="$(java_major_of "$cand" || true)"
        if [ -n "$mj" ] && [ "$mj" -ge "$JAVA_MIN_MAJOR" ]; then
            echo "$cand"; return 0
        fi
    done
    return 1
}

ensure_java() {
    title "检测 Java 运行环境"
    local java_bin
    if java_bin="$(find_java)"; then
        ok "已检测到 Java: $java_bin ($("$java_bin" -version 2>&1 | head -n 1))"
        JAVA_BIN="$java_bin"
        return 0
    fi

    warn "未检测到 Java ${JAVA_MIN_MAJOR}+ 运行时"
    if [ "$AUTO_INSTALL" -eq 0 ]; then
        err "已指定 --no-install，请先手动安装 Java，例如：sudo apt install -y openjdk-17-jre-headless"
        return 1
    fi

    info "尝试自动安装 OpenJDK 运行时..."
    if ! pkg_install openjdk-17-jre-headless 2>/dev/null; then
        info "默认包名安装失败，按发行版重试..."
        case "$(detect_pkg_mgr)" in
            apt)    pkg_install openjdk-17-jre-headless || pkg_install default-jre-headless ;;
            dnf)    pkg_install java-17-openjdk-headless || pkg_install java-latest-openjdk-headless ;;
            yum)    pkg_install java-17-openjdk-headless || pkg_install java-11-openjdk-headless || pkg_install java-1.8.0-openjdk-headless ;;
            apk)    pkg_install openjdk17-jre-headless || pkg_install openjdk11-jre-headless ;;
            pacman) pkg_install jre-openjdk ;;
            zypper) pkg_install java-17-openjdk-headless ;;
            *)      return 1 ;;
        esac
    fi

    if java_bin="$(find_java)"; then
        ok "Java 安装成功: $java_bin"
        JAVA_BIN="$java_bin"
        return 0
    fi

    err "Java 自动安装失败。请手动安装 JRE ${JAVA_MIN_MAJOR}+ 后重新运行。"
    err "  Debian/Ubuntu : sudo apt install -y openjdk-17-jre-headless"
    err "  CentOS/RHEL   : sudo dnf install -y java-17-openjdk-headless"
    err "  Alpine        : sudo apk add openjdk17-jre-headless"
    err "  Arch          : sudo pacman -S jre-openjdk"
    return 1
}

# ---------------------------------------------------------------- Python 检测
find_python() {
    local c
    for c in python3 python; do
        if has_cmd "$c"; then
            # 排除 Python 2
            if "$c" -c 'import sys; sys.exit(0 if sys.version_info[0]==3 else 1)' >/dev/null 2>&1; then
                command -v "$c"; return 0
            fi
        fi
    done
    return 1
}

py_has_module() {
    "$PYTHON_BIN" -c "import $1" >/dev/null 2>&1
}

ensure_python_and_ocr() {
    title "检测 OCR 服务依赖（Python + ddddocr）"
    local py_bin
    if py_bin="$(find_python)"; then
        ok "已检测到 Python: $py_bin ($("$py_bin" --version 2>&1))"
        PYTHON_BIN="$py_bin"
    else
        warn "未检测到 Python 3"
        if [ "$AUTO_INSTALL" -eq 0 ]; then
            err "已指定 --no-install，请先手动安装 Python 3 及 pip"
            return 1
        fi
        info "尝试自动安装 Python 3 + pip..."
        case "$(detect_pkg_mgr)" in
            apt)    pkg_install python3 python3-pip python3-venv ;;
            dnf|yum) pkg_install python3 python3-pip ;;
            apk)    pkg_install python3 py3-pip ;;
            pacman) pkg_install python python-pip ;;
            zypper) pkg_install python3 python3-pip ;;
            *)      return 1 ;;
        esac
        if py_bin="$(find_python)"; then
            ok "Python 安装成功: $py_bin"
            PYTHON_BIN="$py_bin"
        else
            err "Python 自动安装失败，请手动安装 Python 3 后重新运行。"
            return 1
        fi
    fi

    # pip 是否可用
    if ! "$PYTHON_BIN" -m pip --version >/dev/null 2>&1; then
        if [ "$AUTO_INSTALL" -eq 0 ]; then
            err "未检测到 pip，请先安装 python3-pip"
            return 1
        fi
        info "未检测到 pip，尝试自动安装..."
        case "$(detect_pkg_mgr)" in
            apt)    pkg_install python3-pip ;;
            dnf|yum) pkg_install python3-pip ;;
            apk)    pkg_install py3-pip ;;
            pacman) pkg_install python-pip ;;
            zypper) pkg_install python3-pip ;;
        esac
        "$PYTHON_BIN" -m pip --version >/dev/null 2>&1 || {
            err "pip 不可用，请手动安装后重试。"; return 1; }
    fi

    # OCR 三方依赖
    local missing=()
    py_has_module flask      || missing+=("flask")
    py_has_module ddddocr    || missing+=("ddddocr")
    py_has_module requests   || missing+=("requests")

    if [ ${#missing[@]} -eq 0 ]; then
        ok "OCR 依赖已就绪: flask / ddddocr / requests"
        return 0
    fi

    warn "缺少 OCR 依赖: ${missing[*]}"
    if [ "$AUTO_INSTALL" -eq 0 ]; then
        err "已指定 --no-install，请手动执行："
        err "  $PYTHON_BIN -m pip install -r $REQ_FILE"
        return 1
    fi

    info "开始安装 OCR 依赖（可能需要几分钟）..."
    if [ -f "$REQ_FILE" ]; then
        "$PYTHON_BIN" -m pip install -r "$REQ_FILE" || true
    else
        "$PYTHON_BIN" -m pip install "${missing[@]}" || true
    fi

    # 兼容部分发行版的 PEP 668 限制（externally-managed-environment）
    if ! py_has_module ddddocr; then
        info "改用 --break-system-packages 重试..."
        "$PYTHON_BIN" -m pip install --break-system-packages -r "$REQ_FILE" 2>/dev/null \
            || "$PYTHON_BIN" -m pip install --break-system-packages "${missing[@]}" || true
    fi

    local still=()
    py_has_module flask    || still+=("flask")
    py_has_module ddddocr  || still+=("ddddocr")
    py_has_module requests || still+=("requests")
    if [ ${#still[@]} -ne 0 ]; then
        err "OCR 依赖安装失败: ${still[*]}"
        err "可手动执行: $PYTHON_BIN -m pip install -r $REQ_FILE"
        return 1
    fi
    ok "OCR 依赖安装完成"
}

# ---------------------------------------------------------------- 配置检查
ensure_config() {
    title "检测配置文件"
    if [ ! -f "$CONFIG_FILE" ]; then
        if [ -f "${CONFIG_FILE}.example" ]; then
            cp -f "${CONFIG_FILE}.example" "$CONFIG_FILE"
            warn "未找到 $CONFIG_FILE，已从 ${CONFIG_FILE}.example 复制生成，请编辑后再运行。"
            exit 3
        fi
        err "缺少配置文件: $APP_DIR/$CONFIG_FILE"
        err "请创建该文件并填写账号信息（可从 ${CONFIG_FILE}.example 复制）。"
        return 1
    fi
    ok "配置文件: $APP_DIR/$CONFIG_FILE"
}

# ---------------------------------------------------------------- OCR 服务
ocr_start() {
    if nc -z 127.0.0.1 "$OCR_PORT" >/dev/null 2>&1; then
        ok "OCR 服务已在运行 ($OCR_PORT)，跳过启动"
        return 0
    fi
    title "启动 OCR 验证码识别服务"
    nohup "$PYTHON_BIN" "$OCR_SCRIPT" > "logs/ocr.log" 2>&1 &
    OCR_PID=$!
    local i
    for i in $(seq 1 30); do
        sleep 1
        if ! kill -0 "$OCR_PID" 2>/dev/null; then
            err "OCR 服务启动失败，日志如下"
            tail -n 30 "logs/ocr.log" 2>/dev/null
            return 1
        fi
        if nc -z 127.0.0.1 "$OCR_PORT" >/dev/null 2>&1; then
            ok "OCR 服务已启动 (PID $OCR_PID, 端口 $OCR_PORT)"
            return 0
        fi
    done
    err "OCR 服务 30 秒内未就绪，仍继续启动主程序（请自行确认 OCR 可用）。"
    return 0
}

cleanup() {
    if [ -n "$OCR_PID" ] && kill -0 "$OCR_PID" 2>/dev/null; then
        info "关闭 OCR 服务 (PID $OCR_PID)"
        kill "$OCR_PID" 2>/dev/null || true
        wait "$OCR_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

# ================================================================== main
mkdir -p logs

print_banner() {
    printf '%s\n' "=================================================="
    printf '%s\n' "            StudyProxy-Java (Linux)               "
    printf '%s\n' "            英华学堂自动学习工具                     "
    printf '%s\n' "=================================================="
    printf '  工作目录 : %s\n' "$APP_DIR"
    printf '  配置方式 : 外部 %s（可直接编辑，无需重新打包）\n' "$CONFIG_FILE"
    printf '%s\n' "=================================================="
    printf '\n'
}
print_banner

if [ ! -f "$APP_JAR" ]; then
    err "未找到程序文件: $APP_DIR/$APP_JAR"
    exit 1
fi

ensure_java        || exit 1
ensure_config      || exit 1

if [ "$START_OCR" -eq 1 ] && [ -f "$OCR_SCRIPT" ]; then
    ensure_python_and_ocr || { warn "OCR 环境异常，主程序仍将尝试启动（若配置了远程 OCR 可直接使用）。"; }
else
    [ "$START_OCR" -eq 0 ] && info "已指定 --no-ocr，跳过 OCR 服务"
    [ ! -f "$OCR_SCRIPT" ] && [ "$START_OCR" -eq 1 ] && warn "未找到 $OCR_SCRIPT，跳过 OCR 服务"
fi

if [ "$CHECK_ONLY" -eq 1 ]; then
    title "环境检测完成"
    ok "Java   : ${JAVA_BIN:-未检测到}"
    ok "Python : ${PYTHON_BIN:-未检测到}"
    exit 0
fi

if [ "$START_OCR" -eq 1 ] && [ -f "$OCR_SCRIPT" ] && [ -n "${PYTHON_BIN:-}" ]; then
    if has_cmd nc; then
        ocr_start || warn "OCR 服务未就绪，继续启动主程序"
    else
        nohup "$PYTHON_BIN" "$OCR_SCRIPT" > "logs/ocr.log" 2>&1 &
        OCR_PID=$!
        sleep 3
        ok "OCR 服务已提交后台运行 (PID $OCR_PID)，日志: logs/ocr.log"
    fi
fi

title "启动主程序"
info "Java: $JAVA_BIN"
info "JAR : $APP_JAR"
info "日志: $APP_DIR/logs/studyproxy.log"
printf '\n'

# 关闭 TLS 握手对旧服务器的兼容问题；固定 UTF-8 保证中文日志正常
"$JAVA_BIN" \
    -Dfile.encoding=UTF-8 \
    -Dsun.stdout.encoding=UTF-8 \
    -Dsun.stderr.encoding=UTF-8 \
    ${JAVA_OPTS:-} \
    -jar "$APP_JAR" ${APP_ARGS[@]+"${APP_ARGS[@]}"}
RC=$?

printf '\n'
if [ "$RC" -eq 0 ]; then
    ok "程序正常结束 (退出码 0)"
else
    err "程序结束，退出码: $RC"
fi
exit "$RC"
