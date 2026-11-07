# 英华学堂自动学习工具 - Windows版本

## 打包说明

### 方式一：使用打包脚本（推荐）

双击运行 `build-exe.bat`，脚本会自动完成：
1. 清理旧的构建文件
2. 编译项目
3. 打包成Windows可执行文件

### 方式二：使用Maven命令

```bash
mvn clean package -DskipTests
```

## 生成的文件

打包完成后，会在 `target` 目录生成以下文件：

- `StudyProxy.exe` - Windows可执行文件
- `StudyProxy-Java-1.0.0.jar` - Java JAR文件（备用）

## 使用方法

### 1. 准备运行环境

确保系统已安装：
- **Java 8 或更高版本**（必须）
- Windows 7 或更高版本

### 2. 配置文件

将 `config.yaml` 文件放在 `StudyProxy.exe` 同一目录下。

### 3. 运行程序

双击 `StudyProxy.exe` 即可运行。

### 4. OCR服务（可选）

如果需要验证码识别功能，需要单独启动OCR服务：

```bash
python ocr_service.py
```

## 注意事项

1. **首次运行**：程序会在当前目录创建日志文件 `app.log`
2. **配置修改**：修改 `config.yaml` 后需要重启程序
3. **Java版本**：支持 Java 8 到 Java 17
4. **防火墙**：如果系统防火墙弹出提示，请允许程序访问网络

## 目录结构

```
StudyProxy/
├── StudyProxy.exe      # 主程序
├── config.yaml         # 配置文件
├── ocr_service.py      # OCR服务（可选）
├── app.log            # 日志文件（运行后生成）
└── README.md          # 说明文档
```

## 常见问题

### Q: 双击exe后闪退？
A: 请检查：
1. 是否安装了Java环境
2. config.yaml文件是否存在
3. 查看app.log日志文件了解错误信息

### Q: 提示找不到Java？
A: 请安装Java 8或更高版本，并配置JAVA_HOME环境变量

### Q: 验证码识别失败？
A: 需要启动OCR服务：`python ocr_service.py`

### Q: 如何查看运行日志？
A: 查看 `app.log` 文件

## 技术支持

如有问题，请查看 `app.log` 日志文件或联系开发者。
