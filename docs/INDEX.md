# StudyProxy-Java 文档中心

## 文档目录

| 文档 | 说明 | 适用对象 |
|------|------|----------|
| [README.md](../README.md) | 项目简介与快速开始 | 所有用户 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 项目架构设计 | 开发者 |
| [API.md](API.md) | API接口文档 | 开发者 |
| [CONFIGURATION.md](CONFIGURATION.md) | 配置说明文档 | 所有用户 |
| [DEVELOPMENT.md](DEVELOPMENT.md) | 开发指南 | 开发者 |
| [DEPLOYMENT.md](DEPLOYMENT.md) | 部署文档 | 运维人员 |

## 快速导航

### 新手入门

1. 阅读 [README.md](../README.md) 了解项目功能
2. 按照 [README.md](../README.md) 快速开始章节启动项目
3. 参考 [CONFIGURATION.md](CONFIGURATION.md) 配置账号

### 开发者

1. 阅读 [ARCHITECTURE.md](ARCHITECTURE.md) 了解架构设计
2. 参考 [DEVELOPMENT.md](DEVELOPMENT.md) 进行开发
3. 查阅 [API.md](API.md) 了解接口详情

### 运维人员

1. 阅读 [DEPLOYMENT.md](DEPLOYMENT.md) 了解部署方式
2. 参考 [CONFIGURATION.md](CONFIGURATION.md) 配置参数
3. 按需调整并发和性能参数

## 项目结构

```
StudyProxy-Java-yinghua/
├── src/main/java/com/studyproxy/
│   ├── StudyProxyApplication.java    # 主入口
│   ├── action/                       # 业务动作层
│   ├── api/                          # API调用层
│   ├── service/                      # 服务层
│   ├── entity/                       # 实体类
│   ├── config/                       # 配置类
│   ├── enums/                        # 枚举类
│   ├── exception/                    # 异常类
│   └── util/                         # 工具类
├── src/main/resources/
│   ├── application.yml               # Spring配置
│   └── logback-spring.xml            # 日志配置
├── docs/                             # 文档目录
│   ├── INDEX.md                      # 文档索引
│   ├── ARCHITECTURE.md               # 架构文档
│   ├── API.md                        # API文档
│   ├── CONFIGURATION.md              # 配置文档
│   ├── DEVELOPMENT.md                # 开发文档
│   └── DEPLOYMENT.md                 # 部署文档
├── config.yaml                       # 账号配置
├── ocr_service.py                    # OCR服务
├── pom.xml                           # Maven配置
└── README.md                         # 项目说明
```

## 核心功能

### 1. 多账号并行刷课

- 最高支持100账号同时刷课
- 每账号最多5门课程并行学习
- 自动跳过已完成的视频

### 2. 自动验证码识别

- 基于ddddocr的验证码识别
- 识别失败自动重试
- 支持自定义OCR服务

### 3. AI自动答题

- 支持豆包、通义千问等AI
- 支持单选、多选、判断等题型
- 未配置AI时随机答题

### 4. 错误处理

- 账号密码错误自动退出
- 网络请求失败自动重试
- 视频锁定自动跳过

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 应用框架 |
| OkHttp | 4.x | HTTP客户端 |
| FastJSON2 | 2.x | JSON处理 |
| Jsoup | 1.x | HTML解析 |
| ddddocr | Python | 验证码识别 |

## 常见问题

### Q: 如何添加新账号？

编辑 `config.yaml`，在 `accounts.yinghua` 列表中添加：

```yaml
- account: "新账号"
  password: "密码"
  preUrl: "https://school.example.com/"
```

### Q: 如何配置AI答题？

在 `config.yaml` 中配置：

```yaml
accounts:
  settings:
    autoAnswer: true
    aiConfig:
      aiType: "DOUBAO"
      apiKey: "your-api-key"
      model: "doubao-seed-1-6-lite-251015"
```

### Q: 如何调整并发数？

在 `StudyProxyApplication.java` 中修改：

```java
int accountThreads = Math.min(accounts.size(), 100);  // 账号并发
int courseThreads = Math.min(courses.size(), 5);      // 课程并发
```

### Q: 如何查看日志？

```bash
# 实时查看
tail -f logs/study-proxy.log

# 查看错误
grep "ERROR" logs/study-proxy.log
```

## 更新日志

### v1.0.0 (2024-01-15)

- 初始版本发布
- 支持英华学堂刷课
- 支持AI自动答题
- 支持多账号并行

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 联系方式

- 问题反馈: GitHub Issues
- 功能建议: GitHub Discussions

## 免责声明

本项目仅供学习研究使用，请勿用于违反学校规定的行为。使用本工具产生的任何后果由使用者自行承担。
