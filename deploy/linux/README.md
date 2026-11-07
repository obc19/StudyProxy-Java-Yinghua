# StudyProxy-Java — Linux 版

英华学堂视频学习工具（**仅视频学习**，不含考试/作业自动答题）。本目录为精简运行包，**配置文件外置**，修改 `config.yaml` 后直接重启即可生效，无需重新打包。

---

## 1. 目录内容

| 文件 | 说明 |
| --- | --- |
| `StudyProxy-Java-1.0.0.jar` | 主程序（Spring Boot 可执行 JAR，依赖已全部内置） |
| `config.yaml` | 配置文件（账号、刷课参数、OCR 地址等），直接编辑 |
| `config.example.yaml` | 配置模板（含逐字段注释，可作为恢复用的干净副本） |
| `start.sh` | 启动脚本（自动检测/安装 Java、Python 依赖，先起 OCR 再跑主程序） |
| `ocr_service.py` | 验证码识别服务（ddddocr，本地 HTTP 服务，默认 8083 端口） |
| `requirements.txt` | OCR 服务的 Python 依赖 |
| `logs/` | 运行日志目录（`studyproxy.log` / `ocr.log`） |
| `README.md` | 本文件 |

---

## 2. 环境要求

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Linux | CentOS 7.9 / Ubuntu 22.04 及以上 | 已验证发行版 |
| JRE | 8 及以上 | `start.sh` 缺失时自动安装 |
| Python | 3.8+ 与 pip | 缺失时自动安装 |

`start.sh` 支持 apt / dnf / yum / apk / pacman / zypper，覆盖主流发行版。

---

## 3. 使用步骤

### 步骤 1：上传并解压

```bash
tar -zxf StudyProxy-Java-linux.tar.gz
cd StudyProxy-Java-linux
ls
```

### 步骤 2：填写账号

```bash
vi config.yaml
```

只改 `accounts.yinghua` 段，其余保持默认：

```yaml
accounts:
  yinghua:
    - account: "你的学号"
      password: "你的密码"
      preUrl: "https://cqcst.suwankj.com/"   # 必须 / 结尾
    - account: "第二个学号"
      password: "第二个密码"
      preUrl: "https://cqcst.suwankj.com/"
  settings:
    autoStart: true
    interval: 10        # 提交间隔（秒），建议 10-30
```

`vim` 中 `i` 进入编辑，改完 `Esc` → `:wq` 保存退出。

### 步骤 3：环境自检

```bash
chmod +x start.sh
./start.sh --check
```

输出会列出检测到的 Java / Python 路径与配置校验结果。缺依赖时可先用 `--no-install` 观察，再决定是否让脚本自动安装。

### 步骤 4：启动

**前台启动**（调试用，`Ctrl+C` 停止）：

```bash
./start.sh
```

**后台常驻**（推荐，关闭 SSH 后仍继续运行）：

```bash
nohup ./start.sh > logs/nohup.log 2>&1 &
```

查看日志：

```bash
tail -f logs/studyproxy.log      # 主程序
tail -f logs/ocr.log             # 验证码识别服务
```

程序采用高并发执行，单机可同时处理 50+ 账号。

---

## 4. 常用参数

| 参数 | 说明 |
| --- | --- |
| `./start.sh` | 正常启动 |
| `./start.sh --check` | 只检测环境（Java / Python / 配置），不启动 |
| `./start.sh --no-ocr` | 不启动本地 OCR（使用远程或已有 OCR 服务时） |
| `./start.sh --no-install` | 只检测不自动安装，缺依赖直接报错退出 |
| `./start.sh -- <参数>` | `--` 之后的参数原样传给主程序 |
| `./start.sh --help` | 显示帮助 |

环境变量 `JAVA_HOME` 优先于 `PATH` 中的 Java；`JAVA_OPTS` 可追加 JVM 参数：

```bash
JAVA_OPTS="-Xmx1024m" ./start.sh
```

---

## 5. 使用外部 OCR 服务

```yaml
ocr:
  service:
    url: "http://192.168.1.10:8083"
```

改完用 `./start.sh --no-ocr` 启动，避免本机重复拉起 OCR。

---

## 6. 手动分步运行（不使用 start.sh）

```bash
# 1) OCR 服务
python3 -m pip install -r requirements.txt
python3 ocr_service.py &

# 2) 主程序（务必在 config.yaml 所在目录执行）
java -Dfile.encoding=UTF-8 -jar StudyProxy-Java-1.0.0.jar
```

> 程序通过当前工作目录的 `./config.yaml` 读取配置，因此必须在包含该文件的目录中启动。

---

## 7. 停止与清理

```bash
# 前台运行时
Ctrl+C

# 后台运行时
pkill -f StudyProxy-Java-1.0.0.jar
pkill -f ocr_service.py
```

---

## 8. 常见问题

| 现象 | 处理 |
| --- | --- |
| `Permission denied` | `chmod +x start.sh` |
| 提示找不到 Java | `./start.sh --check` 看是否自动安装成功；失败则手动 `apt install openjdk-17-jre-headless` |
| 验证码一直失败 | `ss -lntp \| grep 8083` 确认 OCR 已监听；`tail -50 logs/ocr.log` 看报错 |
| 学时不增长 | `preUrl` 结尾缺 `/`；或 `interval` 过小触发风控，调到 15~30 |
| 日志中文乱码 | 启动加 `-Dfile.encoding=UTF-8`；`start.sh` 已默认带上 |
| 账号密码错误自动退出 | 配置错误，程序主动跳过该账号，属正常保护逻辑 |

---

## 9. 免责声明

本项目仅供**学习研究**使用，请勿用于违反学校规定的行为。使用本工具产生的任何后果由使用者自行承担。请勿用于任何商业用途。
