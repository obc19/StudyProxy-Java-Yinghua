package com.studyproxy.service.ai;

import com.alibaba.fastjson2.JSONArray;
import com.studyproxy.entity.ai.AiChatMessage;
import com.studyproxy.entity.ai.AiConfig;
import com.studyproxy.entity.question.Question;
import com.studyproxy.enums.AiTypeEnum;
import com.studyproxy.enums.QuestionTypeEnum;
import com.studyproxy.service.ai.impl.AiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @InjectMocks
    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testBuildSingleChoiceMessage() {
        Question question = new Question();
        question.setType("单选题");
        question.setContent("Java是什么类型的语言？");
        question.setOptions(Arrays.asList("编译型语言", "解释型语言", "混合型语言", "机器语言"));

        AiChatMessage message = (AiChatMessage) ReflectionTestUtils.invokeMethod(
            aiService, "buildSingleChoiceMessage", question
        );

        assertNotNull(message);
        assertNotNull(message.getMessages());
        assertEquals(2, message.getMessages().size());
        assertTrue(message.getMessages().get(1).getContent().contains("Java是什么类型的语言"));
    }

    @Test
    void testBuildMultipleChoiceMessage() {
        Question question = new Question();
        question.setType("多选题");
        question.setContent("以下哪些是Java的特性？");
        question.setOptions(Arrays.asList("面向对象", "跨平台", "指针操作", "自动垃圾回收"));

        AiChatMessage message = (AiChatMessage) ReflectionTestUtils.invokeMethod(
            aiService, "buildMultipleChoiceMessage", question
        );

        assertNotNull(message);
        assertNotNull(message.getMessages());
        assertEquals(2, message.getMessages().size());
        assertTrue(message.getMessages().get(1).getContent().contains("面向对象"));
    }

    @Test
    void testBuildTrueFalseMessage() {
        Question question = new Question();
        question.setType("判断题");
        question.setContent("Java是一种编译型语言");

        AiChatMessage message = (AiChatMessage) ReflectionTestUtils.invokeMethod(
            aiService, "buildTrueFalseMessage", question
        );

        assertNotNull(message);
        assertNotNull(message.getMessages());
        assertEquals(3, message.getMessages().size());
    }

    @Test
    void testBuildFillBlankMessage() {
        Question question = new Question();
        question.setType("填空题");
        question.setContent("Java的创始人是（answer_1）。");

        AiChatMessage message = (AiChatMessage) ReflectionTestUtils.invokeMethod(
            aiService, "buildFillBlankMessage", question
        );

        assertNotNull(message);
        assertNotNull(message.getMessages());
        assertEquals(3, message.getMessages().size());
    }

    @Test
    void testQuestionTypeEnum() {
        assertEquals(QuestionTypeEnum.SINGLE_CHOICE, QuestionTypeEnum.fromName("单选题"));
        assertEquals(QuestionTypeEnum.MULTIPLE_CHOICE, QuestionTypeEnum.fromName("多选题"));
        assertEquals(QuestionTypeEnum.TRUE_OR_FALSE, QuestionTypeEnum.fromName("判断题"));
        assertEquals(QuestionTypeEnum.FILL_IN_THE_BLANK, QuestionTypeEnum.fromName("填空题"));
        assertEquals(QuestionTypeEnum.OTHER, QuestionTypeEnum.fromName("未知类型"));
    }

    @Test
    void testAiTypeEnum() {
        assertEquals(AiTypeEnum.TONGYI, AiTypeEnum.fromCode("TONGYI"));
        assertEquals(AiTypeEnum.CHATGLM, AiTypeEnum.fromCode("CHATGLM"));
        assertEquals(AiTypeEnum.XINGHUO, AiTypeEnum.fromCode("XINGHUO"));
        assertEquals(AiTypeEnum.OTHER, AiTypeEnum.fromCode("UNKNOWN"));
    }

    @Test
    void testAiConfigCreation() {
        AiConfig config = new AiConfig();
        config.setAiType(AiConfig.AiTypeEnum.TONGYI);
        config.setApiKey("test-api-key");
        config.setModel("qwen-plus-latest");

        assertEquals(AiConfig.AiTypeEnum.TONGYI, config.getAiType());
        assertEquals("test-api-key", config.getApiKey());
        assertEquals("qwen-plus-latest", config.getModel());
    }

    @Test
    void testQuestionCreation() {
        Question question = new Question();
        question.setType("单选题");
        question.setContent("测试题目");
        question.setOptions(Arrays.asList("选项A", "选项B", "选项C"));
        question.setAnswers(Arrays.asList("选项A"));

        assertEquals("单选题", question.getType());
        assertEquals("测试题目", question.getContent());
        assertEquals(3, question.getOptions().size());
        assertEquals(1, question.getAnswers().size());
    }

    @Test
    void testAiChatMessageCreation() {
        AiChatMessage message = new AiChatMessage();
        message.addMessage("system", "你是一个AI助手");
        message.addMessage("user", "请回答问题");

        assertNotNull(message.getMessages());
        assertEquals(2, message.getMessages().size());
        assertEquals("system", message.getMessages().get(0).getRole());
        assertEquals("你是一个AI助手", message.getMessages().get(0).getContent());
    }
}
