package com.studyproxy.entity.question;

import lombok.Data;
import java.util.List;

@Data
public class QuestionResult {
    private Question question;
    private String replier;
    private String message;
    private int code;

    public static QuestionResult success(Question question, String replier) {
        QuestionResult result = new QuestionResult();
        result.setQuestion(question);
        result.setReplier(replier);
        result.setCode(200);
        result.setMessage("找到答案");
        return result;
    }

    public static QuestionResult notFound(Question question) {
        QuestionResult result = new QuestionResult();
        result.setQuestion(question);
        result.setCode(404);
        result.setMessage("未找到答案");
        return result;
    }

    public static QuestionResult error(Question question, String message) {
        QuestionResult result = new QuestionResult();
        result.setQuestion(question);
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
