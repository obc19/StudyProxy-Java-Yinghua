# API接口文档

## 1. 登录相关API

### 1.1 获取验证码

**接口**: `GET /service/code`

**请求参数**: 无

**响应**: 图片二进制数据 (image/png)

**代码位置**: `YinghuaLoginApi.getVerificationCodeImage()`

---

### 1.2 登录

**接口**: `POST /user/login.json`

**请求方式**: multipart/form-data

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | ✅ | 学号/账号 |
| password | String | ✅ | 密码 |
| code | String | ✅ | 验证码 |
| redirect | String | ❌ | 重定向地址 |
| platform | String | ❌ | 平台标识 (Android) |
| version | String | ❌ | 版本号 (1.4.8) |

**响应示例**:

```json
// 成功
{
    "status": true,
    "redirect": "/login?token=sid.xxx&sign=xxx",
    "parse_url": {
        "scheme": "https",
        "host": "school.example.com",
        "path": "/"
    }
}

// 失败 - 验证码错误
{
    "status": false,
    "msg": "验证码有误！",
    "need_code": 1
}

// 失败 - 密码错误
{
    "status": false,
    "msg": "用户名或密码错误"
}
```

**代码位置**: `YinghuaLoginApi.login()`

---

### 1.3 登录保活

**接口**: `POST /api/online.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "在线"
}
```

**代码位置**: `YinghuaLoginApi.keepAlive()`

---

## 2. 课程相关API

### 2.1 获取课程列表

**接口**: `POST /api/course/list.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "获取数据成功",
    "result": {
        "list": [
            {
                "id": 12345,
                "name": "人工智能导论",
                "mode": 1,
                "progress": 85.5,
                "videoCount": 20,
                "videoLearned": 17,
                "startDate": "2024-01-01",
                "endDate": "2024-06-30"
            }
        ],
        "pageInfo": {
            "page": 1,
            "pageSize": 20,
            "pageCount": 1
        }
    }
}
```

**代码位置**: `YinghuaCourseApi.getCourseList()`

---

### 2.2 获取课程详情

**接口**: `POST /api/course/detail.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| courseId | String | ✅ | 课程ID |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "result": {
        "id": 12345,
        "name": "人工智能导论",
        "intro": "课程介绍...",
        "teacher": "张老师",
        "credit": 3.0
    }
}
```

**代码位置**: `YinghuaCourseApi.getCourseDetail()`

---

### 2.3 获取章节/视频列表

**接口**: `POST /api/course/chapter.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| courseId | String | ✅ | 课程ID |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "result": {
        "list": [
            {
                "id": "node001",
                "name": "第一章 绪论",
                "videoDuration": 1200,
                "nodeLock": 0,
                "unlockTime": null,
                "tabVideo": true,
                "tabFile": false,
                "tabVote": false,
                "tabWork": false,
                "tabExam": true
            }
        ]
    }
}
```

**代码位置**: `YinghuaCourseApi.getVideoList()`

---

## 3. 学习相关API

### 3.1 获取学习状态

**接口**: `POST /api/node/study.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| nodeId | String | ✅ | 节点ID |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "result": {
        "studyId": "study001",
        "viewedDuration": 300,
        "videoDuration": 1200
    }
}
```

**代码位置**: `YinghuaStudyApi.getVideoStudyTime()`

---

### 3.2 提交学时

**接口**: `POST /api/node/study.json`

**请求方式**: multipart/form-data

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| nodeId | String | ✅ | 节点ID |
| studyId | String | ✅ | 学习ID |
| viewedDuration | Integer | ✅ | 已观看时长(秒) |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "提交学时成功!",
    "result": {
        "studyId": "study001",
        "data": {
            "studyId": "study002"
        }
    }
}
```

**代码位置**: `YinghuaStudyApi.submitStudyTime()`

---

### 3.3 获取观看记录

**接口**: `POST /api/node/record.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| courseId | String | ✅ | 课程ID |
| page | Integer | ❌ | 页码 (默认1) |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "获取数据成功",
    "result": {
        "list": [
            {
                "id": 123,
                "courseId": 12345,
                "progress": 85.5,
                "viewedDuration": 1020,
                "state": 1
            }
        ],
        "pageInfo": {
            "page": 1,
            "pageSize": 20,
            "pageCount": 1
        }
    }
}
```

**代码位置**: `YinghuaStudyApi.getVideoWatchRecord()`

---

## 4. 考试相关API

### 4.1 开始考试

**接口**: `GET /api/exam/start.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| courseId | String | ✅ | 课程ID |
| nodeId | String | ✅ | 节点ID |
| examId | String | ✅ | 考试ID |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "开始考试"
}
```

**代码位置**: `YinghuaExamApi.startExam()`

---

### 4.2 获取考试题目

**接口**: `POST /api/exam/paper.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| nodeId | String | ✅ | 节点ID |
| examId | String | ✅ | 考试ID |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应**: HTML格式的题目页面

**代码位置**: `YinghuaExamApi.getExamTopics()`

---

### 4.3 提交考试答案

**接口**: `POST /api/exam/answer.json`

**请求方式**: multipart/form-data

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| examId | String | ✅ | 考试ID |
| answerId | String | ✅ | 答案ID |
| question | String | ✅ | 题目内容 |
| answer | String | ✅ | 答案 (格式: [A] 或 [A B C]) |
| isFinish | String | ❌ | 是否完成 (0/1) |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "提交成功"
}
```

**代码位置**: `YinghuaExamApi.submitExamAnswer()`

---

## 5. 作业相关API

### 5.1 获取作业题目

**接口**: `POST /api/work/paper.json`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| nodeId | String | ✅ | 节点ID |
| workId | String | ✅ | 作业ID |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应**: HTML格式的题目页面

**代码位置**: `YinghuaExamApi.getWorkTopics()`

---

### 5.2 提交作业答案

**接口**: `POST /api/work/answer.json`

**请求方式**: multipart/form-data

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | ✅ | 登录token |
| workId | String | ✅ | 作业ID |
| answerId | String | ✅ | 答案ID |
| question | String | ✅ | 题目内容 |
| answer | String | ✅ | 答案 |
| platform | String | ❌ | 平台标识 |
| version | String | ❌ | 版本号 |

**响应示例**:

```json
{
    "status": true,
    "msg": "提交成功"
}
```

**代码位置**: `YinghuaExamApi.submitWorkAnswer()`

---

## 6. OCR服务API

### 6.1 验证码识别

**接口**: `POST /ocr`

**请求方式**: application/json

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| image | String | ✅ | Base64编码的图片 |

**响应示例**:

```json
{
    "code": 200,
    "result": "Abcd"
}
```

**代码位置**: `DdddOcrService.recognize()`

---

### 6.2 健康检查

**接口**: `GET /health`

**响应示例**:

```json
{
    "status": "ok"
}
```

---

## 7. AI答题API

### 7.1 调用AI回答问题

**支持的AI类型**:

| 类型 | 说明 | API地址 |
|------|------|---------|
| DOUBAO | 豆包大模型 | https://ark.cn-beijing.volces.com/api/v3/chat/completions |
| TONGYI | 通义千问 | https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation |
| QWEN | 通义千问 | 同上 |

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| aiType | String | ✅ | AI类型 |
| apiKey | String | ✅ | API密钥 |
| model | String | ✅ | 模型名称 |
| question | Object | ✅ | 题目对象 |

**代码位置**: `AiServiceImpl.answerQuestion()`

---

## 8. 错误码说明

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| status=true | 成功 | 继续执行 |
| status=false | 失败 | 检查msg字段 |
| _code=9 | 考试已开始/超时 | 重新开始 |
| msg="验证码有误" | 验证码错误 | 重新识别 |
| msg="账号登录超时" | Token过期 | 重新登录 |
| msg="用户名或密码错误" | 密码错误 | 程序退出 |
