package com.studyproxy.entity.question;

import lombok.Data;
import java.util.List;

@Data
public class Question {
    private String md5;
    private String type;
    private String content;
    private List<String> options;
    private List<String> answers;

    public Question() {
    }

    public Question(String type, String content, List<String> options) {
        this.type = type;
        this.content = content;
        this.options = options;
    }

    public Question(String type, String content, List<String> options, List<String> answers) {
        this.type = type;
        this.content = content;
        this.options = options;
        this.answers = answers;
    }
}
