# 配置说明文档

## 1. 配置文件

项目使用 `config.yaml` 作为主配置文件，位于项目根目录。

## 2. 完整配置示例

```yaml
# ============================================
# 基础设置 (setting)
# ============================================

setting:
  basicSetting:
    completionTone: 1          # 完成提示音 (0:关闭 1:开启)
    colorLog: 1                # 彩色日志 (0:关闭 1:开启)
    logOutFileSw: 1            # 日志文件输出 (0:关闭 1:开启)
    logLevel: "INFO"           # 日志级别 (DEBUG/INFO/WARN/ERROR)
    logModel: 0                # 日志模式

  emailInform:
    sw: 0                      # 邮件通知开关 (0:关闭 1:开启)
    SMTPHost: ""               # SMTP服务器地址
    SMTPPort: ""               # SMTP端口
    userName: ""               # 邮箱账号
    password: ""               # 邮箱密码/授权码

  aiSetting:
    aiType: "TONGYI"           # AI类型 (TONGYI/DOUBAO/QWEN)
    aiUrl: ""                  # 自定义AI接口地址
    model: ""                  # 模型名称
    API_KEY: ""                # API密钥

  apiQueSetting:
    url: "http://localhost:8083"  # 题库API地址

# ============================================
# 平台配置 (studyproxy)
# ============================================

studyproxy:
  yinghua:
    platform: Android          # 平台标识
    version: 1.4.8             # 客户端版本

  http:
    connect-timeout: 30000     # 连接超时(毫秒)
    read-timeout: 30000        # 读取超时(毫秒)
    write-timeout: 30000       # 写入超时(毫秒)
    retry-times: 3             # 重试次数

# ============================================
# 账号配置 (accounts)
# ============================================

accounts:
  yinghua:
    - account: "2025301409"          # 学号
      password: "password123"        # 密码
      preUrl: "https://school.example.com/"  # 学校平台地址
    - account: "2025301410"
      password: "password456"
      preUrl: "https://school.example.com/"
  settings:
    autoStart: true            # 自动开始学习
    interval: 10               # 提交间隔(秒)
    autoAnswer: true           # 自动答题
    aiConfig:
      aiType: "DOUBAO"         # AI类型
      apiKey: "your-api-key"   # API密钥
      model: "doubao-seed-1-6-lite-251015"  # 模型名称
```

## 3. 配置项详解

### 3.1 基础设置 (setting.basicSetting)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| completionTone | Integer | 1 | 完成提示音开关 |
| colorLog | Integer | 1 | 控制台彩色日志开关 |
| logOutFileSw | Integer | 1 | 日志文件输出开关 |
| logLevel | String | INFO | 日志级别 |
| logModel | Integer | 0 | 日志模式 |

### 3.2 邮件通知 (setting.emailInform)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| sw | Integer | 0 | 邮件通知开关 |
| SMTPHost | String | - | SMTP服务器 |
| SMTPPort | String | - | SMTP端口 |
| userName | String | - | 邮箱账号 |
| password | String | - | 邮箱密码 |

### 3.3 AI设置 (setting.aiSetting)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| aiType | String | TONGYI | AI类型 |
| aiUrl | String | - | 自定义API地址 |
| model | String | - | 模型名称 |
| API_KEY | String | - | API密钥 |

### 3.4 平台配置 (studyproxy.yinghua)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| platform | String | Android | 平台标识 |
| version | String | 1.4.8 | 客户端版本 |

### 3.5 HTTP配置 (studyproxy.http)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| connect-timeout | Integer | 30000 | 连接超时(毫秒) |
| read-timeout | Integer | 30000 | 读取超时(毫秒) |
| write-timeout | Integer | 30000 | 写入超时(毫秒) |
| retry-times | Integer | 3 | 重试次数 |

### 3.6 账号配置 (accounts.yinghua)

| 配置项 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account | String | ✅ | 学号/账号 |
| password | String | ✅ | 密码 |
| preUrl | String | ✅ | 学校平台地址 |

### 3.7 学习设置 (accounts.settings)

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| autoStart | Boolean | true | 自动开始学习 |
| interval | Integer | 10 | 提交间隔(秒) |
| autoAnswer | Boolean | false | 自动答题 |

### 3.8 AI配置 (accounts.settings.aiConfig)

| 配置项 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| aiType | String | ✅ | AI类型 |
| apiKey | String | ✅ | API密钥 |
| model | String | ✅ | 模型名称 |

## 4. AI类型说明

### 4.1 支持的AI类型

| 类型 | 名称 | API地址 |
|------|------|---------|
| DOUBAO | 豆包大模型 | https://ark.cn-beijing.volces.com/api/v3/chat/completions |
| TONGYI | 通义千问 | https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation |
| QWEN | 通义千问 | 同上 |

### 4.2 获取API密钥

#### 豆包大模型
1. 访问 https://www.volcengine.com/
2. 注册/登录账号
3. 开通豆包大模型服务
4. 创建API Key

#### 通义千问
1. 访问 https://dashscope.aliyun.com/
2. 注册/登录阿里云账号
3. 开通DashScope服务
4. 创建API Key

### 4.3 推荐模型

| AI类型 | 推荐模型 | 说明 |
|--------|----------|------|
| DOUBAO | doubao-seed-1-6-lite-251015 | 轻量版，速度快 |
| DOUBAO | doubao-pro-32k | 专业版，效果好 |
| TONGYI | qwen-turbo | 快速版 |
| TONGYI | qwen-plus | 增强版 |
| TONGYI | qwen-max | 旗舰版 |

## 5. 多账号配置

### 5.1 同一学校多账号

```yaml
accounts:
  yinghua:
    - account: "账号1"
      password: "密码1"
      preUrl: "https://school.example.com/"
    - account: "账号2"
      password: "密码2"
      preUrl: "https://school.example.com/"
    - account: "账号3"
      password: "密码3"
      preUrl: "https://school.example.com/"
```

### 5.2 不同学校多账号

```yaml
accounts:
  yinghua:
    - account: "学校1账号"
      password: "密码"
      preUrl: "https://school1.example.com/"
    - account: "学校2账号"
      password: "密码"
      preUrl: "https://school2.example.com/"
    - account: "学校3账号"
      password: "密码"
      preUrl: "https://school3.example.com/"
```

## 6. Spring配置

### 6.1 application.yml

```yaml
spring:
  config:
    import: optional:file:./config.yaml

  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

  messages:
    encoding: UTF-8

ocr:
  service:
    url: http://localhost:5000
```

### 6.2 日志配置 (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss} %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/study-proxy.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/study-proxy.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 7. 配置验证

### 7.1 必填项检查

程序启动时会检查以下必填项：
- 账号列表非空
- 每个账号的 account、password、preUrl 非空

### 7.2 格式验证

- preUrl 必须是有效的URL格式
- preUrl 必须以 `/` 结尾

### 7.3 错误提示

```
[ERROR] 未配置英华学堂账号，请检查config.yaml
[ERROR] 账号配置缺少必填字段: account
[ERROR] preUrl格式错误，必须以/结尾
```

## 8. 配置热更新

当前版本不支持配置热更新，修改配置后需要重启应用。

## 9. 配置安全

### 9.1 密码加密

建议使用环境变量存储敏感信息：

```yaml
accounts:
  yinghua:
    - account: "${ACCOUNT_1}"
      password: "${PASSWORD_1}"
      preUrl: "https://school.example.com/"
```

运行时设置环境变量：

```bash
export ACCOUNT_1="2025301409"
export PASSWORD_1="password123"
java -jar target/StudyProxy-Java-1.0.0.jar
```

### 9.2 配置文件权限

```bash
chmod 600 config.yaml
```

## 10. 常见配置问题

### Q1: 配置文件找不到？

确保 `config.yaml` 在项目根目录，或通过参数指定：

```bash
java -jar target/StudyProxy-Java-1.0.0.jar --spring.config.location=file:./config.yaml
```

### Q2: AI答题不生效？

检查配置：
1. `autoAnswer` 是否为 `true`
2. `aiConfig` 是否正确配置
3. `apiKey` 是否有效

### Q3: 多账号配置不生效？

检查YAML格式，确保正确缩进：

```yaml
accounts:
  yinghua:
    - account: "账号1"    # 正确缩进
      password: "密码1"
    - account: "账号2"
      password: "密码2"
```
