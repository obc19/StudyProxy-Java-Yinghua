# 开发指南

## 1. 开发环境搭建

### 1.1 环境要求

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 1.8+ | Java运行环境 |
| Maven | 3.6+ | 项目构建 |
| Python | 3.8+ | OCR服务 |
| IDE | IDEA/Eclipse | 开发工具 |

### 1.2 克隆项目

```bash
git clone https://github.com/xxx/StudyProxy-Java-yinghua.git
cd StudyProxy-Java-yinghua
```

### 1.3 安装依赖

```bash
mvn clean install
```

### 1.4 启动OCR服务

```bash
pip install ddddocr flask
python ocr_service.py
```

### 1.5 运行项目

```bash
mvn spring-boot:run
```

## 2. 项目结构

```
StudyProxy-Java-yinghua/
├── src/
│   ├── main/
│   │   ├── java/com/studyproxy/
│   │   │   ├── StudyProxyApplication.java    # 主入口
│   │   │   ├── action/                       # 业务动作层
│   │   │   ├── api/                          # API调用层
│   │   │   ├── service/                      # 服务层
│   │   │   ├── entity/                       # 实体类
│   │   │   ├── config/                       # 配置类
│   │   │   ├── enums/                        # 枚举类
│   │   │   ├── exception/                    # 异常类
│   │   │   └── util/                         # 工具类
│   │   └── resources/
│   │       ├── application.yml               # Spring配置
│   │       └── logback-spring.xml            # 日志配置
│   └── test/                                 # 测试代码
├── docs/                                     # 文档目录
├── config.yaml                               # 账号配置
├── ocr_service.py                            # OCR服务
├── pom.xml                                   # Maven配置
└── README.md                                 # 项目说明
```

## 3. 核心类说明

### 3.1 入口类

#### StudyProxyApplication.java

主入口类，负责：
- Spring Boot启动
- 多账号并行调度
- 控制台输出

```java
@SpringBootApplication
public class StudyProxyApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyProxyApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner consoleRunner(...) {
        return args -> {
            // 1. 加载账号配置
            // 2. 创建账号线程池
            // 3. 并行处理每个账号
            // 4. 等待完成
        };
    }
}
```

### 3.2 动作层

#### YinghuaLoginAction.java

登录动作，负责：
- 验证码获取
- OCR识别
- 登录验证
- 密码错误检测

```java
@Component
public class YinghuaLoginAction {
    public String login(YinghuaUserCache userCache) {
        // 1. 获取验证码图片
        byte[] captchaImage = yinghuaLoginApi.getVerificationCodeImage(userCache);
        
        // 2. OCR识别
        String verCode = ddddOcrService.recognize(captchaImage);
        
        // 3. 提交登录
        String jsonStr = yinghuaLoginApi.login(userCache);
        
        // 4. 检测密码错误
        if (isPasswordError(jsonStr)) {
            System.exit(1);
        }
        
        // 5. 提取token
        userCache.setToken(token);
        return jsonStr;
    }
}
```

#### YinghuaCourseAction.java

课程动作，负责：
- 课程列表获取
- 视频列表获取
- 课程详情获取

```java
@Component
public class YinghuaCourseAction {
    public List<YinghuaCourse> pullCourseList(YinghuaUserCache userCache) {
        return yinghuaCourseApi.getCourseList(userCache);
    }
    
    public List<YinghuaNode> pullVideoList(YinghuaUserCache userCache, String courseId) {
        return yinghuaCourseApi.getVideoList(userCache, courseId);
    }
}
```

### 3.3 服务层

#### YinghuaVideoStudyService.java

视频学习服务，核心刷课逻辑：
- 视频学习
- 考试处理
- 作业处理

```java
@Service
public class YinghuaVideoStudyService {
    public void studyCourse(YinghuaUserCache userCache, YinghuaCourse course) {
        List<YinghuaNode> nodeList = getFullVideoList(userCache, course.getId());
        
        for (YinghuaNode node : nodeList) {
            // 处理视频
            if (node.getTabVideo()) {
                studyVideo(userCache, node);
            }
            // 处理考试
            if (node.getTabExam()) {
                handleExam(userCache, course.getId(), node, aiConfig);
            }
            // 处理作业
            if (node.getTabWork()) {
                handleWork(userCache, course.getId(), node, aiConfig);
            }
        }
    }
}
```

#### YinghuaAiService.java

AI答题服务：
- 考试自动答题
- 作业自动答题
- 题目类型识别

```java
@Service
public class YinghuaAiService {
    public void autoAnswerExam(YinghuaUserCache userCache, String courseId, 
                              String nodeId, String examId, AiConfig aiConfig) {
        // 1. 开始考试
        yinghuaExamApi.startExam(userCache, courseId, nodeId, examId);
        
        // 2. 获取题目
        String topicHtml = yinghuaExamApi.getExamTopics(userCache, nodeId, examId);
        List<ExamTopic> topics = yinghuaExamApi.parseExamTopics(topicHtml);
        
        // 3. 逐题回答
        for (ExamTopic topic : topics) {
            Question question = convertToQuestion(topic);
            QuestionResult result = answerWithAi(aiConfig, question);
            yinghuaExamApi.submitExamAnswer(userCache, examId, topic.getAnswerId(), 
                topic.getContent(), formatAnswer(topic));
        }
    }
}
```

### 3.4 API层

#### YinghuaLoginApi.java

登录相关API：
- `getVerificationCodeImage()` - 获取验证码
- `login()` - 登录
- `keepAlive()` - 保活

#### YinghuaCourseApi.java

课程相关API：
- `getCourseList()` - 获取课程列表
- `getCourseDetail()` - 获取课程详情
- `getVideoList()` - 获取视频列表

#### YinghuaStudyApi.java

学习相关API：
- `getVideoStudyTime()` - 获取学习状态
- `submitStudyTime()` - 提交学时
- `getVideoWatchRecord()` - 获取观看记录

#### YinghuaExamApi.java

考试相关API：
- `startExam()` - 开始考试
- `getExamTopics()` - 获取题目
- `submitExamAnswer()` - 提交答案
- `parseExamTopics()` - 解析题目

### 3.5 实体类

#### YinghuaUserCache.java

用户缓存，存储登录状态：

```java
@Data
public class YinghuaUserCache {
    private String preUrl;        // 平台地址
    private String account;       // 账号
    private String password;      // 密码
    private String verCode;       // 验证码
    private InMemoryCookieJar cookieJar;  // Cookie
    private String token;         // Token
    private String sign;          // 签名
}
```

#### YinghuaCourse.java

课程实体：

```java
@Data
public class YinghuaCourse {
    private String id;            // 课程ID
    private String name;          // 课程名称
    private Float progress;       // 学习进度
    private Integer videoCount;   // 视频总数
    private Integer videoLearned; // 已学视频数
}
```

#### YinghuaNode.java

节点实体：

```java
@Data
public class YinghuaNode {
    private String id;            // 节点ID
    private String name;          // 节点名称
    private Integer videoDuration;  // 视频时长
    private Float progress;       // 学习进度
    private Integer viewedDuration; // 已观看时长
    private Boolean tabVideo;     // 是否有视频
    private Boolean tabExam;      // 是否有考试
    private Boolean tabWork;      // 是否有作业
}
```

## 4. 扩展开发

### 4.1 添加新的AI类型

1. 在 `AiTypeEnum.java` 添加枚举：

```java
public enum AiTypeEnum {
    DOUBAO("豆包", "DOUBAO"),
    TONGYI("通义", "TONGYI"),
    NEW_AI("新AI", "NEW_AI");  // 新增
    
    private final String name;
    private final String code;
}
```

2. 在 `AiServiceImpl.java` 添加实现：

```java
@Override
public String answerQuestion(AiConfig config, Question question) {
    switch (config.getAiType()) {
        case DOUBAO:
            return callDoubao(config, question);
        case TONGYI:
            return callTongyi(config, question);
        case NEW_AI:
            return callNewAi(config, question);  // 新增
        default:
            throw new RuntimeException("不支持的AI类型");
    }
}

private String callNewAi(AiConfig config, Question question) {
    // 实现新AI的调用逻辑
}
```

### 4.2 添加新的题目类型

1. 在 `QuestionTypeEnum.java` 添加枚举：

```java
public enum QuestionTypeEnum {
    SINGLE_CHOICE("单选题"),
    MULTIPLE_CHOICE("多选题"),
    TRUE_OR_FALSE("判断题"),
    FILL_BLANK("填空题"),
    SHORT_ANSWER("简答题"),
    NEW_TYPE("新题型");  // 新增
}
```

2. 在 `YinghuaAiService.java` 添加处理逻辑：

```java
private QuestionResult answerWithRandom(Question question) {
    switch (typeEnum) {
        case SINGLE_CHOICE:
            // 单选逻辑
        case NEW_TYPE:
            return answerNewType(question);  // 新增
    }
}
```

### 4.3 添加新的平台

1. 创建新的包结构：

```
src/main/java/com/studyproxy/
├── api/newplatform/
│   ├── NewPlatformLoginApi.java
│   ├── NewPlatformCourseApi.java
│   └── NewPlatformStudyApi.java
├── service/newplatform/
│   └── NewPlatformStudyService.java
├── entity/newplatform/
│   ├── NewPlatformUserCache.java
│   └── NewPlatformCourse.java
└── action/
    └── NewPlatformLoginAction.java
```

2. 在 `PlatformEnum.java` 添加枚举：

```java
public enum PlatformEnum {
    YINGHUA("英华学堂", "YINGHUA"),
    NEW_PLATFORM("新平台", "NEW_PLATFORM");  // 新增
}
```

3. 在 `StudyProxyApplication.java` 添加处理：

```java
@Bean
public CommandLineRunner consoleRunner(...) {
    return args -> {
        // 根据平台类型选择处理逻辑
        if (platform == PlatformEnum.YINGHUA) {
            // 英华处理
        } else if (platform == PlatformEnum.NEW_PLATFORM) {
            // 新平台处理
        }
    };
}
```

## 5. 调试技巧

### 5.1 日志级别配置

在 `application.yml` 中配置：

```yaml
logging:
  level:
    com.studyproxy: DEBUG
    com.studyproxy.api: TRACE  # 查看API请求详情
```

### 5.2 单元测试

```java
@SpringBootTest
public class YinghuaLoginActionTest {
    @Resource
    private YinghuaLoginAction yinghuaLoginAction;
    
    @Test
    public void testLogin() {
        YinghuaUserCache userCache = new YinghuaUserCache();
        userCache.setAccount("test");
        userCache.setPassword("test");
        userCache.setPreUrl("https://school.example.com/");
        
        String result = yinghuaLoginAction.login(userCache);
        assertNotNull(result);
        assertNotNull(userCache.getToken());
    }
}
```

### 5.3 抓包调试

使用 Fiddler/Charles 抓包：
1. 配置代理
2. 查看请求/响应
3. 分析API参数

## 6. 常见问题

### Q1: 如何添加新的API接口？

1. 在对应的Api类中添加方法
2. 使用 `HttpUtil` 发送请求
3. 解析响应并返回

### Q2: 如何修改并发数？

在 `StudyProxyApplication.java` 中修改：

```java
int accountThreads = Math.min(accounts.size(), 100);  // 账号并发数
int courseThreads = Math.min(courses.size(), 5);      // 课程并发数
```

### Q3: 如何自定义答题逻辑？

继承 `YinghuaAiService` 并重写方法：

```java
@Service
public class CustomAiService extends YinghuaAiService {
    @Override
    protected QuestionResult answerWithAi(AiConfig aiConfig, Question question) {
        // 自定义答题逻辑
    }
}
```

## 7. 发布部署

### 7.1 打包

```bash
mvn clean package -DskipTests
```

### 7.2 运行

```bash
java -jar target/StudyProxy-Java-1.0.0.jar
```

### 7.3 后台运行

```bash
nohup java -jar target/StudyProxy-Java-1.0.0.jar > output.log 2>&1 &
```

### 7.4 Docker部署

```dockerfile
FROM openjdk:8-jre
WORKDIR /app
COPY target/StudyProxy-Java-1.0.0.jar app.jar
COPY config.yaml config.yaml
CMD ["java", "-jar", "app.jar"]
```

```bash
docker build -t study-proxy .
docker run -d --name study-proxy study-proxy
```
