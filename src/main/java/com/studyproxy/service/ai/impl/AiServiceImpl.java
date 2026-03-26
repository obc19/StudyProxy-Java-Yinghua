package com.studyproxy.service.ai.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.entity.ai.AiChatMessage;
import com.studyproxy.entity.ai.AiConfig;
import com.studyproxy.entity.question.Question;
import com.studyproxy.enums.AiTypeEnum;
import com.studyproxy.enums.QuestionTypeEnum;
import com.studyproxy.service.ai.AiService;
import com.studyproxy.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Resource
    private HttpUtil httpUtil;

    private static final Semaphore AI_SEMAPHORE = new Semaphore(2);

    private static final int MAX_RETRY = 7;

    @Override
    public String chat(AiConfig config, AiChatMessage message) throws Exception {
        AI_SEMAPHORE.acquire();
        try {
            switch (config.getAiType()) {
                case TONGYI:
                    return tongYiChat(config, message, MAX_RETRY, null);
                case CHATGLM:
                    return chatGLMChat(config, message, MAX_RETRY, null);
                case XINGHUO:
                    return xingHuoChat(config, message, MAX_RETRY, null);
                case DOUBAO:
                    return douBaoChat(config, message, MAX_RETRY, null);
                case OPENAI:
                    return openAiChat(config, message, MAX_RETRY, null);
                case DEEPSEEK:
                    return deepSeekChat(config, message, MAX_RETRY, null);
                case SILICON:
                    return siliconFlowChat(config, message, MAX_RETRY, null);
                case OTHER:
                    return otherChat(config, message, MAX_RETRY, null);
                default:
                    throw new RuntimeException("不支持的AI类型: " + config.getAiType());
            }
        } finally {
            AI_SEMAPHORE.release();
        }
    }

    @Override
    public String answerQuestion(AiConfig config, Question question) throws Exception {
        AiChatMessage message = buildQuestionMessage(question);
        return chat(config, message);
    }

    @Override
    public boolean check(AiConfig config) throws Exception {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("user", "请你原模原样输出：[\"测试成功\"]");
        try {
            String result = chat(config, message);
            return result != null && result.contains("测试成功");
        } catch (Exception e) {
            log.error("AI检查失败: {}", e.getMessage());
            return false;
        }
    }

    private AiChatMessage buildQuestionMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        QuestionTypeEnum typeEnum = QuestionTypeEnum.fromName(question.getType());

        switch (typeEnum) {
            case SINGLE_CHOICE:
                return buildSingleChoiceMessage(question);
            case MULTIPLE_CHOICE:
                return buildMultipleChoiceMessage(question);
            case TRUE_OR_FALSE:
                return buildTrueFalseMessage(question);
            case FILL_IN_THE_BLANK:
                return buildFillBlankMessage(question);
            case SHORT_ANSWER:
                return buildShortAnswerMessage(question);
            case TERM_EXPLANATION:
                return buildTermExplanationMessage(question);
            case ESSAY:
                return buildEssayMessage(question);
            case MATCHING:
                return buildMatchingMessage(question);
            default:
                return buildDefaultMessage(question);
        }
    }

    private AiChatMessage buildSingleChoiceMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "接下来无论出现任何题目，你都必须只回答题目中某个选项对应的内容，并严格按照以下要求作答：\n" +
                "\n" +
                "【回答规则】\n" +
                "1. 最终输出必须严格遵循 JSON 数组格式，例如：[\"选项内容\"]\n" +
                "2. 数组中只能有一个字符串元素。\n" +
                "3. 字符串中不能包含选项前缀，如 A. B. C. D. 等，只能输出选项的纯内容。\n" +
                "4. 不能输出解析、解释步骤、理由、提示语或任何多余文本。\n" +
                "5. 不能输出题目本身、不能输出其他格式，只能输出 JSON 数组。\n" +
                "6. 如果你无法判断正确答案，也必须随机选择一个选项的内容进行输出，不允许回答\"我不知道\"\"无法判断\"之类内容。\n" +
                "\n" +
                "【格式要求】\n" +
                "- 只能输出 JSON\n" +
                "- 不允许换行，若内容中需要换行必须使用\\n\n" +
                "- 不能出现额外的空格、标点或第二层数组");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：单选题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        String[] options = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        List<String> optionList = question.getOptions();
        for (int i = 0; i < optionList.size() && i < options.length; i++) {
            problem.append(options[i]).append(".").append(optionList.get(i)).append("\n");
        }

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildMultipleChoiceMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "接下来无论出现任何题目，你都必须只回答选项对应的内容，且必须严格按照以下格式输出：\n" +
                "\n" +
                "【最终输出格式】\n" +
                "[\"选项内容1\",\"选项内容2\", ...]\n" +
                "- JSON 数组只能包含字符串元素。\n" +
                "- 每个元素对应一个被选中的选项内容。\n" +
                "- 严禁携带 A. B. C. D. 等前缀，只能输出纯内容。\n" +
                "- 不得输出解析、解释、思考过程、题目内容或任何无关文本。\n" +
                "- 如果你无法判断正确选项，也必须随机选择多个选项内容填入数组。");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：多选题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        String[] options = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        List<String> optionList = question.getOptions();
        for (int i = 0; i < optionList.size() && i < options.length; i++) {
            problem.append(options[i]).append(".").append(optionList.get(i)).append("\n");
        }

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildTrueFalseMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "接下来你只需要回答\"正确\"或者\"错误\"即可...格式：[\"正确\"]");
        message.addMessage("system", "就算你不知道选什么也随机选...无需回答任何解释！！！");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：判断题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");
        problem.append("A.正确\n").append("B.错误\n");

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildFillBlankMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "其中，\"（answer_数字）\"相关字样的地方是你需要填写答案的地方，回答时请严格遵循json格式：[\"答案1\",\"答案2\"]");
        message.addMessage("system", "就算你不知道选什么也随机选...无需回答任何解释！！！");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：填空题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildShortAnswerMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "这是一个简答题，回答时请严格遵循json格式，包括换行等特殊符号也要遵循json语法：[\"答案\"]，注意不要拆分答案！！！");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：简答题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildTermExplanationMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "最终输出必须是一个合法 JSON 数组格式：[\"答案内容\"]\n" +
                "数组中只能包含一个字符串元素，答案必须完整写在同一个字符串里，不能拆分成多个元素。\n" +
                "字符串内如需换行必须写为 \\n，不能出现真正的换行符。\n" +
                "字符串内如出现双引号必须转义为 \\\"。\n" +
                "答案内容必须是连贯的完整论述，不得包含解析、题目、注释或生成说明。\n" +
                "除 JSON 数组外严禁输出任何其他内容。");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：名词解释\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildEssayMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "最终输出必须是一个合法 JSON 数组格式：[\"答案内容\"]\n" +
                "数组中只能包含一个字符串元素，答案必须完整写在同一个字符串里，不能拆分成多个元素。\n" +
                "字符串内如需换行必须写为 \\n，不能出现真正的换行符。\n" +
                "字符串内如出现双引号必须转义为 \\\"。\n" +
                "答案内容必须是连贯的完整论述，不得包含解析、题目、注释或生成说明。\n" +
                "答案字数不少于 500 字。\n" +
                "除 JSON 数组外严禁输出任何其他内容。");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：论述题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildMatchingMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "接下来你只需要以json格式回答选项对应内容即可，比如：[\"xxx->xxx\",\"xxx->xxx\"]");
        message.addMessage("system", "就算你不知道选什么也随机按指定要求格式回答...无需回答任何解释！！！");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：连线题\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        List<String> optionList = question.getOptions();
        problem.append("组别一：\n");
        for (String option : optionList) {
            if (option.startsWith("[1]")) {
                problem.append(option.replace("[1]", "")).append("\n");
            }
        }

        problem.append("组别二：\n");
        for (String option : optionList) {
            if (option.startsWith("[2]")) {
                problem.append(option.replace("[2]", "")).append("\n");
            }
        }

        message.addMessage("user", problem.toString());
        return message;
    }

    private AiChatMessage buildDefaultMessage(Question question) {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "请回答以下问题，格式：[\"答案\"]");

        StringBuilder problem = new StringBuilder();
        problem.append("题目类型：").append(question.getType()).append("\n");
        problem.append("题目内容：\n").append(question.getContent()).append("\n");

        message.addMessage("user", problem.toString());
        return message;
    }

    private String tongYiChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("通义千问调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        String model = config.getModel() != null ? config.getModel() : "qwen-plus-latest";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "通义千问");
        } catch (Exception e) {
            Thread.sleep(100);
            return tongYiChat(config, message, retry - 1, e);
        }
    }

    private String chatGLMChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("ChatGLM调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        String model = config.getModel() != null ? config.getModel() : "glm-4";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "ChatGLM");
        } catch (Exception e) {
            Thread.sleep(100);
            return chatGLMChat(config, message, retry - 1, e);
        }
    }

    private String xingHuoChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("讯飞星火调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://spark-api-open.xf-yun.com/v1/chat/completions";
        String model = config.getModel() != null ? config.getModel() : "generalv3.5";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "讯飞星火");
        } catch (Exception e) {
            Thread.sleep(100);
            return xingHuoChat(config, message, retry - 1, e);
        }
    }

    private String douBaoChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("字节豆包调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://ark.cn-beijing.volces.com/api/v3/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "字节豆包");
        } catch (Exception e) {
            Thread.sleep(100);
            return douBaoChat(config, message, retry - 1, e);
        }
    }

    private String openAiChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("OpenAI调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://api.openai.com/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "OpenAI");
        } catch (Exception e) {
            Thread.sleep(100);
            return openAiChat(config, message, retry - 1, e);
        }
    }

    private String deepSeekChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("DeepSeek调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://api.deepseek.com/chat/completions";
        String model = config.getModel() != null ? config.getModel() : "deepseek-chat";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "DeepSeek");
        } catch (Exception e) {
            Thread.sleep(100);
            return deepSeekChat(config, message, retry - 1, e);
        }
    }

    private String siliconFlowChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("SiliconFlow调用失败", lastError);
        }

        String url = config.getUrl() != null ? config.getUrl() : "https://api.siliconflow.cn/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(url, JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "SiliconFlow");
        } catch (Exception e) {
            Thread.sleep(100);
            return siliconFlowChat(config, message, retry - 1, e);
        }
    }

    private String otherChat(AiConfig config, AiChatMessage message, int retry, Exception lastError) throws Exception {
        if (retry < 0) {
            throw new RuntimeException("自定义AI调用失败", lastError);
        }

        if (config.getUrl() == null) {
            throw new RuntimeException("自定义AI必须提供URL");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", 0.2);
        requestBody.put("messages", message.getMessages());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        headers.put("Content-Type", "application/json");

        try {
            String response = httpUtil.postJson(config.getUrl(), JSON.toJSONString(requestBody), headers);
            return parseAiResponse(response, config, message, retry, "自定义AI");
        } catch (Exception e) {
            Thread.sleep(100);
            return otherChat(config, message, retry - 1, e);
        }
    }

    private String parseAiResponse(String response, AiConfig config, AiChatMessage message, int retry, String aiName) throws Exception {
        JSONObject jsonResponse = JSON.parseObject(response);

        if (jsonResponse.containsKey("message")) {
            String msg = jsonResponse.getString("message");
            if (msg != null && msg.contains("Request processing has failed")) {
                throw new RuntimeException(aiName + "处理失败: " + response);
            }
        }

        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException(aiName + "返回内容为空: " + response);
        }

        JSONObject choice = choices.getJSONObject(0);
        JSONObject messageObj = choice.getJSONObject("message");
        if (messageObj == null) {
            throw new RuntimeException(aiName + "消息解析失败: " + response);
        }

        String content = messageObj.getString("content");
        if (content == null) {
            throw new RuntimeException(aiName + "内容为空: " + response);
        }

        try {
            JSONArray.parseArray(content);
            return content;
        } catch (Exception e) {
            message.addMessage("system", content);
            message.addMessage("user", "你刚才生成的回复未严格遵循json格式，我无法正常解析，请你重新生成。");
            return chat(config, message);
        }
    }
}
