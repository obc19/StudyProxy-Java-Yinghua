# StudyProxy-Java — Windows 版

英华学堂视频学习工具（**仅视频学习**，不含考试/作业自动答题）。

本目录是开箱即用运行包：`config.yaml` 外置，改完重启即生效，不需要重新打包。

---

## 1. 目录内容

| 文件 | 说明 |
| --- | --- |
| `StudyProxy.exe` | 主程序（launch4j 封装的自包含可执行文件，需系统已装 JRE） |
| `config.yaml` | 配置文件（账号、刷课参数、OCR 地址），用记事本/VS Code 编辑 |
| `start-all.bat` | 一键启动脚本：检查 Java/Python → 装 OCR 依赖 → 起 OCR → 起主程序 |
| `ocr_service.py` | 验证码识别服务（ddddocr，本地 HTTP 服务，默认 8083 端口） |
| `requirements.txt` | OCR 服务的 Python 依赖 |
| `logs\` | 运行日志目录 |
| `README.md` | 本文件 |

---

## 2. 环境要求

| 组件 | 版本 | 是否必需 |
| --- | --- | --- |
| Windows | 7 SP1 / 10 / 11（64 位） | 必需 |
| JRE / JDK | **8 ~ 21**（推荐 17） | 必需，未装则 exe 无法启动 |
| Python | 3.8+ | 可选，但**验证码识别必须**，否则登录失败 |

- Java 下载：<https://adoptium.net/temurin/releases/>（选 Temurin 17，x64，`.msi` 安装时勾选 *Set JAVA_HOME*）
- Python 下载：<https://www.python.org/downloads/>（安装时务必勾选 **Add python.exe to PATH**）

---

## 3. 使用步骤

### 步骤 1：解压

把 `StudyProxy-Java-windows.zip` 解压到一个**不含中文和空格的短路径**，例如 `D:\StudyProxy`。

> 路径含中文一般也能跑，但日志与 OCR 输出可能乱码，建议避开。

### 步骤 2：填写账号

用记事本或 VS Code 打开 `config.yaml`，只改 `accounts.yinghua` 这一段：

```yaml
accounts:
  yinghua:
    - account: "你的学号"
      password: "你的密码"
      preUrl: "https://cqcst.suwankj.com/"   # 必须 / 结尾
    - account: "第二个学号"
      password: "第二个密码"
      preUrl: "https://cqcst.suwankj.com/"
```

- `preUrl`：学校英华学堂地址，**必须带结尾的 `/`**。在浏览器打开学校登录页，地址栏 `https://xxx.suwankj.com/` 这一段（不要带 `/#/login` 等路径）。
- 账号数量不限，每个账号并行执行。
- `accounts.settings.interval`：提交学时间隔（秒），建议 10~30，过小可能触发服务端风控。
- 保存时确认编码为 **UTF-8**（记事本「另存为」选 UTF-8）。

### 步骤 3：启动

**方式一（推荐）**：双击 `start-all.bat`

脚本会依次执行：

1. 检查 `config.yaml` 是否存在；
2. 检查 Java（无则提示安装并退出）；
3. 检查 `flask / ddddocr / requests`，缺失则自动 `pip install -r requirements.txt`；
4. 最小化窗口启动 OCR 服务（端口 8083）；
5. 启动 `StudyProxy.exe`。

**方式二**：手动分步

```bat
python -m pip install -r requirements.txt
python ocr_service.py
StudyProxy.exe
```

> 必须在 `config.yaml` 所在目录启动，程序只读当前工作目录下的 `./config.yaml`。

### 步骤 4：确认运行

主程序窗口会打印登录、课程列表、学习进度等日志；同时 `logs\` 下会生成日志文件。
全部课程学完后按提示音（`completionTone: 1`）结束。

---

## 4. 配置文件要点

```yaml
setting:
  basicSetting:
    completionTone: 1      # 完成提示音 0/1
    colorLog: 1            # 彩色日志 0/1
    logLevel: "INFO"       # DEBUG / INFO / WARN / ERROR
    logModel: 0            # 0=简洁 1=详细
ocr:
  service:
    url: "http://localhost:8083"   # OCR 服务地址
accounts:
  yinghua: [...]
  settings:
    autoStart: true        # 启动后自动开始
    interval: 10           # 提交间隔（秒）
```

完整字段说明见仓库 `docs/CONFIGURATION.md`。

---

## 5. 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 双击 exe 闪退 | ① 未装 JRE，装 JDK 17；② 缺 `config.yaml`；③ 看 `logs\` 或 `app.log` |
| 提示“卡在登录 / 验证码错误” | OCR 服务没起，双击 `start-all.bat`，或手动 `python ocr_service.py` 看是否 8083 被占用 |
| `pip install ddddocr` 失败 | 需要 Python 3.8+ 且网络可用；国内可加 `-i https://pypi.tuna.tsinghua.edu.cn/simple` |
| 账号密码错误自动退出 | 配置错误，程序会主动退出该账号，属正常保护逻辑 |
| 中文日志乱码 | 系统代码页问题，`chcp 65001` 后再运行，或直接看日志文件 |
| 学时不增长 | `preUrl` 结尾缺 `/`，或 `interval` 过小被风控，调到 15~30 重试 |

---

## 6. 免责声明

本项目仅供**学习研究**使用，请勿用于违反学校规定的行为。使用本工具产生的任何后果由使用者自行承担。请勿用于任何商业用途。
