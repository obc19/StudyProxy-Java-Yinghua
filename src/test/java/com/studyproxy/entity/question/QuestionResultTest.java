package com.studyproxy.entity.question;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class QuestionResultTest {

    @Test
    void testSuccessResult() {
        Question question = new Question();
        question.setType("单选题");
        question.setContent("测试题目");
        question.setOptions(Arrays.asList("A", "B", "C"));
        question.setAnswers(Arrays.asList("A"));

        QuestionResult result = QuestionResult.success(question, "AI助手");

        assertEquals(200, result.getCode());
        assertEquals("找到答案", result.getMessage());
        assertEquals("AI助手", result.getReplier());
        assertNotNull(result.getQuestion());
    }

    @Test
    void testNotFoundResult() {
        Question question = new Question();
        question.setType("单选题");
        question.setContent("测试题目");

        QuestionResult result = QuestionResult.notFound(question);

        assertEquals(404, result.getCode());
        assertEquals("未找到答案", result.getMessage());
        assertNotNull(result.getQuestion());
    }

    @Test
    void testErrorResult() {
        Question question = new Question();
        question.setType("单选题");
        question.setContent("测试题目");

        QuestionResult result = QuestionResult.error(question, "处理失败");

        assertEquals(500, result.getCode());
        assertEquals("处理失败", result.getMessage());
        assertNotNull(result.getQuestion());
    }

    @Test
    void testQuestionWithAllFields() {
        Question question = new Question();
        question.setMd5("test-md5-hash");
        question.setType("多选题");
        question.setContent("以下哪些是正确的？");
        question.setOptions(Arrays.asList("选项A", "选项B", "选项C", "选项D"));
        question.setAnswers(Arrays.asList("选项A", "选项C"));

        assertEquals("test-md5-hash", question.getMd5());
        assertEquals("多选题", question.getType());
        assertEquals("以下哪些是正确的？", question.getContent());
        assertEquals(4, question.getOptions().size());
        assertEquals(2, question.getAnswers().size());
    }
}
