package com.studyproxy.entity.ai;

import lombok.Data;
import java.util.List;

@Data
public class AiChatMessage {
    private List<Message> messages;

    @Data
    public static class Message {
        private String role;
        private String content;
    }

    public AiChatMessage() {
    }

    public AiChatMessage(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(String role, String content) {
        if (this.messages == null) {
            this.messages = new java.util.ArrayList<>();
        }
        Message message = new Message();
        message.setRole(role);
        message.setContent(content);
        this.messages.add(message);
    }
}
