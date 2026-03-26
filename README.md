# StudyProxy-Java

英华学堂自动学习工具 - Java版

## 功能特性

- ✅ 多账号并行刷课（最高100账号）
- ✅ 多课程并行学习（每账号5课程）
- ✅ 自动验证码识别（ddddocr）
- ✅ 视频自动学习
- ✅ 考试/作业自动答题（AI支持）
- ✅ 账号密码错误自动退出
- ✅ 配置文件驱动

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- Python 3.8+ (OCR服务)

### 2. 安装OCR服务

```bash
pip install ddddocr flask
python ocr_service.py
```

### 3. 配置账号

编辑 `config.yaml`：

```yaml
accounts:
  yinghua:
    - account: "账号1"
      password: "密码1"
      preUrl: "https://school.example.com/"
    - account: "账号2"
      password: "密码2"
      preUrl: "https://school.example.com/"
  settings:
    autoStart: true
    interval: 10
    autoAnswer: true
    aiConfig:
      aiType: "DOUBAO"
      apiKey: "your-api-key"
      model: "doubao-seed-1-6-lite-251015"
```

### 4. 运行

```bash
mvn spring-boot:run
```

或打包运行：

```bash
mvn clean package
java -jar target/StudyProxy-Java-1.0.0.jar
```

## 配置说明

### 账号配置

| 字段 | 说明 | 必填 |
|------|------|------|
| account | 学号/账号 | ✅ |
| password | 密码 | ✅ |
| preUrl | 学校平台地址 | ✅ |

### AI配置

| 字段 | 说明 | 必填 |
|------|------|------|
| aiType | AI类型: DOUBAO/TONGYI/QWEN | ✅ |
| apiKey | API密钥 | ✅ |
| model | 模型名称 | ✅ |

### 设置项

| 字段 | 说明 | 默认值 |
|------|------|--------|
| autoStart | 自动开始 | true |
| interval | 提交间隔(秒) | 10 |
| autoAnswer | 自动答题 | true |

## 项目结构

```
src/main/java/com/studyproxy/
├── StudyProxyApplication.java      # 主入口
├── action/                          # 业务动作层
│   ├── YinghuaLoginAction.java     # 登录动作
│   ├── YinghuaCourseAction.java    # 课程动作
│   └── YinghuaStudyAction.java     # 学习动作
├── api/yinghua/                     # API调用层
│   ├── YinghuaLoginApi.java        # 登录API
│   ├── YinghuaCourseApi.java       # 课程API
│   ├── YinghuaStudyApi.java        # 学习API
│   └── YinghuaExamApi.java         # 考试API
├── service/                         # 服务层
│   ├── yinghua/
│   │   ├── YinghuaVideoStudyService.java  # 视频刷课服务
│   │   └── YinghuaAiService.java          # AI答题服务
│   ├── ocr/
│   │   └── DdddOcrService.java     # OCR服务
│   └── ai/
│       └── AiService.java          # AI接口
├── entity/yinghua/                  # 实体类
│   ├── YinghuaUserCache.java       # 用户缓存
│   ├── YinghuaCourse.java          # 课程实体
│   ├── YinghuaNode.java            # 节点实体
│   ├── YinghuaExam.java            # 考试实体
│   └── YinghuaWork.java            # 作业实体
├── config/                          # 配置类
│   ├── YinghuaConfig.java          # 英华配置
│   └── StudyProxyConfig.java       # 全局配置
└── util/                            # 工具类
    ├── HttpUtil.java               # HTTP工具
    └── JsonUtil.java               # JSON工具
```

## 运行日志示例

```
========================================
       StudyProxy-Java v1.0.0           
       英华学堂自动学习工具              
========================================

[INFO] 共配置 2 个账号，开始并行刷课...
[INFO] [账号1/2] 2025301409 开始处理
[INFO] [2025301409] 正在登录...
[INFO] [2025301409] 第1次尝试登录 - 获取验证码
[INFO] [2025301409] 验证码识别结果: 'Abcd'
[INFO] [2025301409] 登录成功
[INFO] [2025301409] 获取到 5 门课程
[INFO] [2025301409] 开始并行刷课...
[INFO] [2025301409] 开始学习视频: 第一章 绪论
[INFO] [2025301409] [第一章 绪论] 进度: 30/120秒 (25.0%)
[INFO] [2025301409] [第一章 绪论] 进度: 60/120秒 (50.0%)
[INFO] [2025301409] 开始处理考试: 第一章测试
[INFO] [2025301409] 考试 [第一章测试] 完成
[INFO] [2025301409] 课程 [人工智能导论] 完成 - 视频:5/5 考试:1/1 作业:0/0
[INFO] ========== 所有账号刷课完成 ==========
```

## 常见问题

### Q: 验证码识别失败？

确保OCR服务已启动：
```bash
python ocr_service.py
# 访问 http://localhost:5000/health 检查
```

### Q: 账号密码错误？

程序会自动检测并退出：
```
[ERROR] [账号] 账号或密码错误，程序退出！
```

### Q: 如何配置多个学校？

每个账号可以配置不同的 `preUrl`：
```yaml
accounts:
  yinghua:
    - account: "账号1"
      preUrl: "https://school1.example.com/"
    - account: "账号2"
      preUrl: "https://school2.example.com/"
```

## 免责声明

本项目仅供学习研究使用，请勿用于违反学校规定的行为。使用本工具产生的任何后果由使用者自行承担。

## License

MIT License
