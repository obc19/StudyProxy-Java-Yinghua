# StudyProxy-Java

英华学堂**自动视频学习工具 · Java 版**

多账号并行、多课程并行、验证码自动识别，配置文件驱动，Windows / Linux 双平台开箱可用。

---

## 下载可执行包（普通用户看这里）

**不需要编译、不需要装 Maven，解压填账号就能跑。**

👉 **下载页：<https://github.com/obc19/StudyProxy-Java-Yinghua/releases/latest>**

| 平台 | 直接下载 | 解压后如何启动 |
| --- | --- | --- |
| Windows 7/10/11 (64 位) | [StudyProxy-Java-windows.zip](https://github.com/obc19/StudyProxy-Java-Yinghua/releases/download/v1.0.0/StudyProxy-Java-windows.zip) | 双击 `start-all.bat` |
| Linux (CentOS / Ubuntu 等) | [StudyProxy-Java-linux.tar.gz](https://github.com/obc19/StudyProxy-Java-Yinghua/releases/download/v1.0.0/StudyProxy-Java-linux.tar.gz) | `tar -zxf ... && cd StudyProxy-Java-linux && chmod +x start.sh && ./start.sh` |

> ⚠️ **不要点页面上的 `Code` → `Download ZIP`，页底的 `Source code (zip/tar.gz)` 也不要下**
> —— 那是**源码快照**，拿到的是 `src/`、`pom.xml` 这些代码，必须自己装 JDK + Maven 编译才能用。
> 你要的「下载就能用」的包，只在上面 **Releases → Assets** 里。

---

> **版本说明**：当前版本**只做视频学习**。考试与作业的自动答题功能已整体移除，
> 配置文件中的 `autoAnswer` / `aiConfig` 也已删除。若你手里的旧配置含这两项，删除即可，程序会忽略未知字段。

---

## 目录

- [下载可执行包（普通用户看这里）](#下载可执行包普通用户看这里)
- [功能特性](#功能特性)
- [一、如何使用本仓库（三条路径）](#一如何使用本仓库三条路径)
- [二、Windows 使用步骤](#二windows-使用步骤)
- [三、Linux 使用步骤](#三linux-使用步骤)
- [四、配置文件说明](#四配置文件说明)
- [五、从源码构建](#五从源码构建)
- [六、仓库结构与文件说明](#六仓库结构与文件说明)
- [七、常见问题](#七常见问题)
- [八、参考文档](#八参考文档)
- [九、二次开发与引用说明](#九二次开发与引用说明)
- [免责声明](#免责声明)

---

## 功能特性

| 能力 | 说明 |
| --- | --- |
| 多账号并行 | `accounts.yinghua` 下写多少账号就并行跑多少，互不影响 |
| 多课程并行 | 每个账号默认并发处理 5 门课程 |
| 闯关式课程适配 | 支持按顺序解锁的课程，自动等待前置章节完成 |
| 验证码识别 | 内置 ddddocr 本地 OCR 服务，登录验证码自动识别 |
| 登录失败保护 | 账号或密码错误时自动退出该账号，不阻塞其它账号 |
| 配置外置 | 所有行为由 `config.yaml` 驱动，改完重启即生效，不需要重新打包 |
| 双平台发布包 | Windows 单文件 `StudyProxy.exe`、Linux 精简 tar 包（自动装依赖） |

---

## 一、如何使用本仓库（三条路径）

先按自己的需求选一条，三条路径最终都需要**一个填好账号的 `config.yaml`**。

### 路径 A：直接用发布包（推荐给普通用户，不需要开发工具）

**首选 [Releases 下载页](https://github.com/obc19/StudyProxy-Java-Yinghua/releases/latest)**，Assets 里就是构建好的可执行包。仓库 `dist/` 目录只作为镜像备份（从 Code → Download ZIP 拿到的副本里也含这两个包，但体积大、不推荐）：

| 平台 | 文件 | 大小 | 内容 |
| --- | --- | --- | --- |
| Windows 7/10/11 | `StudyProxy-Java-windows.zip` | ≈18 MB | `StudyProxy.exe` + 配置 + 启动脚本 + OCR 服务 |
| Linux（CentOS / Ubuntu 等） | `StudyProxy-Java-linux.tar.gz` | ≈18 MB | 可执行 JAR + 配置 + `start.sh` + OCR 服务 |

下载对应平台的包 → 解压 → 改 `config.yaml` 里的账号 → 运行启动脚本。详见第二、三节。

### 路径 B：从源码构建（需要 JDK + Maven）

```bash
git clone https://github.com/obc19/StudyProxy-Java-Yinghua.git
cd StudyProxy-Java-Yinghua
mvn clean package -DskipTests          # 产物：target/StudyProxy-Java-1.0.0.jar
```

Windows 额外产出 `target/StudyProxy.exe`（`windows-exe` profile 自动启用 launch4j）。
详见[第五节](#五从源码构建)。

### 路径 C：只要配置模板或 OCR 服务

- 配置模板：仓库根目录 `config.example.yaml`（含逐字段中文注释），复制为 `config.yaml` 后改账号即可。
- OCR 服务：`ocr_service.py` + `requirements.txt`，可单独部署为 HTTP 服务，任何程序都能调用：

```bash
pip install -r requirements.txt
python ocr_service.py            # 默认监听 0.0.0.0:8083

curl http://localhost:8083/health
# POST /ocr  body: base64 图片字符串，或 multipart/form-data 字段名 image
```

---

## 二、Windows 使用步骤

### 2.1 环境准备

| 组件 | 版本 | 必需性 |
| --- | --- | --- |
| Windows | 7 SP1 / 10 / 11（64 位） | 必需 |
| JRE / JDK | **8 ~ 21**（推荐 17） | 必需，缺了 exe 无法启动 |
| Python | 3.8+ | 建议安装，否则无法识别验证码 |

- Java：<https://adoptium.net/temurin/releases/> 选 **Temurin 17 x64 .msi**，安装时勾选 *Set JAVA_HOME variable*。
- Python：<https://www.python.org/downloads/> 安装时务必勾选 **Add python.exe to PATH**。

### 2.2 解压

把 `StudyProxy-Java-windows.zip` 解压到**不含中文和空格**的短路径，例如 `D:\StudyProxy`。
（中文路径通常也能运行，但控制台日志可能乱码。）

解压后目录：

```
StudyProxy-Java-windows\
├── StudyProxy.exe
├── config.yaml
├── config.example.yaml
├── start-all.bat
├── ocr_service.py
├── requirements.txt
├── logs\
└── README.md
```

### 2.3 填写账号

用记事本或 VS Code 打开 `config.yaml`，只改 `accounts.yinghua` 段：

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
    autoStart: true     # 启动后自动开刷
    interval: 10        # 提交间隔（秒），建议 10-30
```

- `preUrl` 是学校英华学堂的**站点根地址**，必须带结尾 `/`；浏览器登录页为 `https://xxx.suwankj.com/#/login` 时，填 `https://xxx.suwankj.com/`。
- 账号数量不限，全部并行。
- 保存时确认编码为 **UTF-8**。

### 2.4 启动

**推荐**：双击 `start-all.bat`，脚本自动完成：

1. 校验 `config.yaml` 是否存在；
2. 检测 Java（缺失则提示安装地址并退出）；
3. 检测 `flask / ddddocr / requests`，缺失自动 `pip install -r requirements.txt`；
4. 最小化窗口启动 OCR 服务（8083）；
5. 启动 `StudyProxy.exe`。

**手动分步**：

```bat
python -m pip install -r requirements.txt
python ocr_service.py        :: 保持窗口打开，监听 8083
StudyProxy.exe
```

> 必须在 `config.yaml` 所在目录启动：程序读取的是当前工作目录下的 `./config.yaml`。
> 直接双击 `StudyProxy.exe` 也可以，但需自行保证 OCR 已运行。

### 2.5 观察运行

主程序窗口持续输出登录、课程列表、学习进度；`logs\` 下生成日志。
全部课程完成后按 `completionTone` 播放提示音。

---

## 三、Linux 使用步骤

### 3.1 上传与解压

```bash
tar -zxf StudyProxy-Java-linux.tar.gz
cd StudyProxy-Java-linux
ls
```

### 3.2 填写账号

```bash
vi config.yaml
```

修改 `accounts.yinghua`（字段含义同 Windows 一节），`Esc` → `:wq` 保存。

### 3.3 环境自检

```bash
chmod +x start.sh
./start.sh --check
```

会打印检测到的 Java / Python 路径与配置校验结果。

### 3.4 启动

前台（调试）：

```bash
./start.sh
```

后台常驻（推荐，断开 SSH 不中断）：

```bash
nohup ./start.sh > logs/nohup.log 2>&1 &
```

查看日志：

```bash
tail -f logs/studyproxy.log     # 主程序
tail -f logs/ocr.log            # OCR 服务
```

`start.sh` 会自动：检测并安装 JRE（apt/dnf/yum/apk/pacman/zypper）、检测 Python3 + pip、
安装 OCR 依赖、后台拉起 OCR、运行主程序，退出时回收自己拉起的 OCR 进程。

### 3.5 常用参数

| 参数 | 说明 |
| --- | --- |
| `./start.sh` | 正常启动 |
| `./start.sh --check` | 只检测环境，不启动 |
| `./start.sh --no-ocr` | 不启动本地 OCR（用远程 OCR 时） |
| `./start.sh --no-install` | 只检测不自动安装 |
| `./start.sh -- <参数>` | `--` 后的参数透传给主程序 |
| `./start.sh --help` | 帮助 |

环境变量：`JAVA_HOME` 优先于 `PATH`；`JAVA_OPTS` 追加 JVM 参数。

```bash
JAVA_OPTS="-Xmx1024m" ./start.sh
```

### 3.6 停止

```bash
pkill -f StudyProxy-Java-1.0.0.jar
pkill -f ocr_service.py
```

---

## 四、配置文件说明

`config.yaml` 与程序同级，完整模板见 [`config.example.yaml`](config.example.yaml)。

### 4.1 结构总览

```yaml
setting:
  basicSetting:   { completionTone, colorLog, logOutFileSw, logLevel, logModel }
  emailInform:    { sw, SMTPHost, SMTPPort, userName, password }   # 暂未使用
  apiQueSetting:  { url }                                          # OCR 地址（旧字段）
ocr:
  service:        { url }                                          # OCR 地址（当前生效）
studyproxy:
  yinghua:        { platform, version }
  http:           { connect-timeout, read-timeout, write-timeout, retry-times }
accounts:
  yinghua:        [ { account, password, preUrl }, ... ]
  settings:       { autoStart, interval }
```

### 4.2 关键字段

| 字段 | 默认 | 说明 |
| --- | --- | --- |
| `accounts.yinghua[].account` | — | 学号 / 登录用户名 |
| `accounts.yinghua[].password` | — | 登录密码（明文，请勿提交到公开仓库） |
| `accounts.yinghua[].preUrl` | — | 学校站点根地址，**必须以 `/` 结尾** |
| `accounts.settings.autoStart` | `true` | 启动后自动开刷；`false` 为手动开始 |
| `accounts.settings.interval` | `10` | 提交学时间隔（秒），建议 10~30，过小易被风控 |
| `ocr.service.url` | `http://localhost:8083` | OCR 服务地址，远程部署时改成 `http://IP:8083` |
| `setting.basicSetting.logLevel` | `INFO` | `DEBUG` / `INFO` / `WARN` / `ERROR` |
| `setting.basicSetting.logModel` | `0` | `0` 简洁、`1` 详细 |
| `setting.basicSetting.completionTone` | `1` | 完成提示音开关 |
| `studyproxy.http.retry-times` | `3` | 单请求失败重试次数 |
| `studyproxy.yinghua.version` | `1.4.8` | 模拟的 APP 版本号，平台升级后如有异常可调整 |

> 安全提示：`config.yaml` 含明文账号密码，**不要提交到公开仓库**。本仓库已通过
> `.gitignore` 排除 `config.yaml`，只提交 `config.example.yaml`。

---

## 五、从源码构建

### 5.1 环境要求

- JDK 8+（编译目标 1.8，实测 JDK 21 可构建）
- Maven 3.6+
- Python 3.8+（仅运行 OCR 时需要）

### 5.2 构建命令

```bash
mvn clean package -DskipTests
```

产物：

| 文件 | 说明 |
| --- | --- |
| `target/StudyProxy-Java-1.0.0.jar` | Spring Boot 可执行 JAR，所有依赖内置 |
| `target/StudyProxy.exe` | Windows 单文件可执行（launch4j 封装同一 JAR） |

### 5.3 仓库自带构建脚本

| 脚本 | 用途 |
| --- | --- |
| `build-exe.bat` | Windows：清理 → 编译 → 生成 `StudyProxy.exe` |
| `build-linux.sh` | Linux：编译并生成 `dist/StudyProxy-Java-linux/` 与 `.tar.gz` |
| `build-linux.ps1` | Windows 上交叉生成 Linux 精简包 |
| `create-release.bat` / `create-release.ps1` | 组装通用 release 目录 |
| `dist/make_zip_release.py` | 把 `dist/StudyProxy-Java-windows/` 打成 zip |
| `dist/make_tar_release.py` | 把 `dist/StudyProxy-Java-linux/` 打成 tar.gz（保留 `start.sh` 可执行位） |

### 5.4 运行（开发态）

```bash
mvn spring-boot:run
# 或在 config.yaml 所在目录
java -Dfile.encoding=UTF-8 -jar target/StudyProxy-Java-1.0.0.jar
```

---

## 六、仓库结构与文件说明

```
StudyProxy-Java-Yinghua/
├── pom.xml                      Maven 配置（Spring Boot 2.7.18 / Java 8）
├── config.example.yaml          配置模板（提交入库，含完整注释）
├── src/main/java/com/studyproxy/
│   ├── StudyProxyApplication.java   启动类
│   ├── action/                 业务动作（登录、选课、学习调度）
│   ├── api/yinghua/            英华学堂 HTTP 接口封装
│   ├── service/                业务服务（含 service/ocr 验证码识别）
│   ├── config/                 配置绑定与初始化
│   ├── entity/ dto/ enums/     数据模型
│   └── util/                   工具类
├── src/main/resources/
│   ├── application.yml         引入外部 ./config.yaml
│   └── logback-spring.xml      日志格式
├── docs/                       架构、API、配置、部署、开发文档
├── dist/                       发布包
│   ├── StudyProxy-Java-windows.zip
│   ├── StudyProxy-Java-linux.tar.gz
│   └── make_*.py               打包脚本
├── ocr_service.py              ddddocr 验证码识别 HTTP 服务（8083）
├── requirements.txt            OCR 依赖
├── release-linux/              Linux 启动脚本源码（start.sh / make_tar.py 等）
├── start-all.bat               Windows 启动脚本
└── Dockerfile / docker-compose.yml
```

---

## 七、常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 双击 exe 闪退 | 未装 JRE；或缺 `config.yaml`；或看 `logs\`、`app.log` 的错误栈 |
| 一直卡在登录 / 验证码错误 | OCR 未启动或被占用：`ss -lntp \| grep 8083`；看 `logs/ocr.log` |
| `pip install ddddocr` 失败 | 需 Python 3.8+ 且能访问 PyPI；国内可加 `-i https://pypi.tuna.tsinghua.edu.cn/simple` |
| 学时不增长 | `preUrl` 缺少结尾 `/`；或 `interval` 过小被风控，调到 15~30 |
| 提示账号密码错误后退出 | 配置错误或密码变更，程序主动跳过该账号，属正常保护 |
| 中文日志乱码 | 控制台代码页问题；直接看日志文件，或 `chcp 65001` 后重跑；Linux 下 `-Dfile.encoding=UTF-8` 已内置 |
| Java 版本不兼容 | EXE 限定 JRE 1.8~21；更高版本请用 `java -jar` 直接跑 JAR |
| 想多机分摊账号 | 拆分 `config.yaml` 到多份分别启动；或共用一台远程 OCR（`--no-ocr`） |

---

## 八、参考文档

| 文档 | 内容 |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | 架构设计与模块划分 |
| [docs/API.md](docs/API.md) | 平台接口封装说明 |
| [docs/CONFIGURATION.md](docs/CONFIGURATION.md) | 配置项详解 |
| [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) | 部署方式（含 Docker） |
| [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) | 开发与调试指南 |
| [docs/INDEX.md](docs/INDEX.md) | 文档索引 |

---

## 九、二次开发与引用说明

本仓库采用 MIT 协议，**允许自由使用、修改、分发**，包括个人学习与二次开发。若引用或衍生，建议：

1. 保留 `LICENSE` 与本 README，并在项目说明中注明来源：
   > 本项目基于 [obc19/StudyProxy-Java-Yinghua](https://github.com/obc19/StudyProxy-Java-Yinghua) 修改。
2. **不要把真实账号密码提交到仓库**。复制 `config.example.yaml` 为 `config.yaml` 后填写，`config.yaml` 已被 `.gitignore` 排除。
3. 修改平台对接逻辑优先改 `src/main/java/com/studyproxy/api/yinghua/`；业务调度在 `action/` 与 `service/`，避免把平台差异散落到各处。
4. 重新发布前确认打包产物中**不含**本地 `config.yaml`（仓库的打包脚本使用模板替换，不会带走真实账号）。

---

## 免责声明

本项目仅供**学习研究**使用，请勿用于违反学校规定的行为。使用本工具产生的任何后果由使用者自行承担。**请勿用于任何商业用途。**

## License

[MIT License](LICENSE)
