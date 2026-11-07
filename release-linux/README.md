# StudyProxy-Java — Linux 版

英华学堂视频学习工具（仅视频，不含考试/作业自动答题）。本目录为精简运行包，**配置文件外置**，修改 `config.yaml` 后直接重启即可生效，无需重新打包。

## 目录内容

| 文件 | 说明 |
| --- | --- |
| `StudyProxy-Java-1.0.0.jar` | 主程序（Spring Boot 可执行 JAR，依赖已全部内置） |
| `config.yaml` | 配置文件（账号、刷课参数、AI 配置、OCR 地址等），可直接编辑 |
| `start.sh` | 启动脚本（自动检测/安装 Java、Python 依赖，先起 OCR 再跑主程序） |
| `ocr_service.py` | 验证码识别服务（ddddocr，本地 HTTP 服务，默认 8083 端口） |
| `requirements.txt` | OCR 服务的 Python 依赖 |
| `README.md` | 本文件 |

## 快速开始

```bash
chmod +x start.sh     # 首次使用赋予执行权限
./start.sh
```

首次运行 `start.sh` 会依次检测：

1. **Java**：需要 JRE 8 及以上。缺失时自动用系统包管理器安装（apt/dnf/yum/apk/pacman/zypper）。
2. **Python 3 + pip**：缺失时自动安装。
3. **OCR 依赖**：`flask`、`ddddocr`、`requests`，缺失时自动 `pip install -r requirements.txt`。
4. **配置文件**：`config.yaml` 缺失时给出提示并以退出码 3 结束。

检测通过后，脚本会先在后台拉起 OCR 服务（日志 `logs/ocr.log`），再启动主程序（日志 `logs/studyproxy.log`）。主程序退出时，脚本会自动关闭自己拉起的 OCR 进程。

## 常用参数

| 参数 | 说明 |
| --- | --- |
| `./start.sh` | 正常启动 |
| `./start.sh --check` | 只检测环境（Java / Python / 配置），不启动 |
| `./start.sh --no-ocr` | 不启动本地 OCR（使用远程或已有的 OCR 服务时） |
| `./start.sh --no-install` | 只检测不自动安装，缺依赖直接报错退出 |
| `./start.sh -- <参数>` | `--` 之后的参数原样传给主程序 |
| `./start.sh --help` | 显示帮助 |

环境变量 `JAVA_HOME` 优先于 `PATH` 中的 Java；`JAVA_OPTS` 可追加 JVM 参数，例如：

```bash
JAVA_OPTS="-Xmx1024m" ./start.sh
```

## 配置文件要点

`config.yaml` 中与运行直接相关的部分：

```yaml
accounts:
  yinghua:
    - account: "学号"
      password: "密码"
      preUrl: "https://cqcst.suwankj.com/"   # 必须以 / 结尾
  settings:
    interval: 10        # 提交学时间隔（秒）
```

> 本版本只做视频学习，已不含 `autoAnswer` / `aiConfig` 配置项。

OCR 服务地址（默认本地 8083）：

```yaml
ocr:
  service:
    url: "http://localhost:8083"
```

如果使用别的机器上的 OCR 服务，把 `ocr.service.url` 改成对应地址，并用 `./start.sh --no-ocr` 启动即可。

## 手动分步运行（可选）

不依赖 `start.sh` 时：

```bash
# 1) 启动 OCR 服务
python3 -m pip install -r requirements.txt
python3 ocr_service.py &

# 2) 启动主程序（务必在 config.yaml 所在目录执行）
java -Dfile.encoding=UTF-8 -jar StudyProxy-Java-1.0.0.jar
```

> 注意：程序通过当前工作目录下的 `./config.yaml` 读取配置，请在包含该文件的目录中启动。

## 停止

在主程序窗口按 `Ctrl+C`；若 OCR 服务残留在后台：

```bash
pkill -f ocr_service.py
```

## 免责声明

本项目仅供学习研究使用，请勿用于违反学校规定的行为。使用本工具产生的任何后果由使用者自行承担。请勿进行任何商业行为。
