package com.studyproxy.service.ai;

import com.studyproxy.entity.ai.AiChatMessage;
import com.studyproxy.entity.ai.AiConfig;
import com.studyproxy.entity.question.Question;

public interface AiService {
    
    String chat(AiConfig config, AiChatMessage message) throws Exception;
    
    String answerQuestion(AiConfig config, Question question) throws Exception;
    
    boolean check(AiConfig config) throws Exception;
}
