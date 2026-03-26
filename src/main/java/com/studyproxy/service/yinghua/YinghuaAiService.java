package com.studyproxy.service.yinghua;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaExamApi;
import com.studyproxy.entity.ai.AiConfig;
import com.studyproxy.entity.question.Question;
import com.studyproxy.entity.question.QuestionResult;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.enums.QuestionTypeEnum;
import com.studyproxy.service.ai.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class YinghuaAiService {

    @Resource
    private YinghuaExamApi yinghuaExamApi;

    @Resource
    private AiService aiService;

    public void autoAnswerExam(YinghuaUserCache userCache, String courseId, String nodeId, 
                              String examId, AiConfig aiConfig, boolean autoSubmit) {
        try {
            String startResult = yinghuaExamApi.startExam(userCache, courseId, nodeId, examId);
            JSONObject startJson = JSONObject.parseObject(startResult);
            
            if (startJson.getInteger("_code") != null && startJson.getInteger("_code") == 9) {
                throw new RuntimeException(startJson.getString("msg"));
            }

            String topicHtml = yinghuaExamApi.getExamTopics(userCache, nodeId, examId);
            List<YinghuaExamApi.ExamTopic> topics = yinghuaExamApi.parseExamTopics(topicHtml);

            for (YinghuaExamApi.ExamTopic topic : topics) {
                Question question = convertToQuestion(topic);
                
                QuestionResult result;
                if (aiConfig != null && aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty()) {
                    result = answerWithAi(aiConfig, question);
                } else {
                    result = answerWithRandom(question);
                }

                if (result.getCode() == 200 && result.getQuestion().getAnswers() != null) {
                    topic.setAnswers(result.getQuestion().getAnswers());
                    
                    String answerStr = formatAnswer(topic);
                    String submitResult = yinghuaExamApi.submitExamAnswer(
                        userCache, examId, topic.getAnswerId(), 
                        topic.getContent(), answerStr, "0"
                    );
                    
                    log.info("题目提交结果: {}", submitResult);
                }
            }

            if (autoSubmit) {
                log.info("考试答题完成，自动提交");
            } else {
                log.info("考试答题完成，请手动提交");
            }
        } catch (Exception e) {
            log.error("自动答题失败: {}", e.getMessage(), e);
            throw new RuntimeException("自动答题失败: " + e.getMessage(), e);
        }
    }

    public void autoAnswerWork(YinghuaUserCache userCache, String nodeId, String workId, AiConfig aiConfig) {
        try {
            String topicHtml = yinghuaExamApi.getWorkTopics(userCache, nodeId, workId);
            List<YinghuaExamApi.ExamTopic> topics = yinghuaExamApi.parseExamTopics(topicHtml);

            for (YinghuaExamApi.ExamTopic topic : topics) {
                Question question = convertToQuestion(topic);
                
                QuestionResult result;
                if (aiConfig != null && aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty()) {
                    result = answerWithAi(aiConfig, question);
                } else {
                    result = answerWithRandom(question);
                }

                if (result.getCode() == 200 && result.getQuestion().getAnswers() != null) {
                    topic.setAnswers(result.getQuestion().getAnswers());
                    
                    String answerStr = formatAnswer(topic);
                    String submitResult = yinghuaExamApi.submitWorkAnswer(
                        userCache, workId, topic.getAnswerId(),
                        topic.getContent(), answerStr
                    );
                    
                    log.info("作业题目提交结果: {}", submitResult);
                }
            }

            log.info("作业答题完成");
        } catch (Exception e) {
            log.error("作业自动答题失败: {}", e.getMessage(), e);
            throw new RuntimeException("作业自动答题失败: " + e.getMessage(), e);
        }
    }

    private Question convertToQuestion(YinghuaExamApi.ExamTopic topic) {
        Question question = new Question();
        
        String type = convertQuestionType(topic.getType());
        question.setType(type);
        question.setContent(topic.getContent());
        question.setOptions(topic.getOptions());
        
        return question;
    }

    private String convertQuestionType(String originalType) {
        if (originalType == null) {
            return "其它";
        }
        
        if (originalType.contains("单选")) {
            return "单选题";
        } else if (originalType.contains("多选")) {
            return "多选题";
        } else if (originalType.contains("判断")) {
            return "判断题";
        } else if (originalType.contains("填空")) {
            return "填空题";
        } else if (originalType.contains("简答")) {
            return "简答题";
        } else if (originalType.contains("论述")) {
            return "论述题";
        } else if (originalType.contains("名词解释")) {
            return "名词解释";
        }
        
        return "其它";
    }

    private QuestionResult answerWithAi(AiConfig aiConfig, Question question) {
        try {
            String answer = aiService.answerQuestion(aiConfig, question);
            
            if (answer == null || answer.isEmpty()) {
                return QuestionResult.notFound(question);
            }

            List<String> answers = JSONArray.parseArray(answer, String.class);
            question.setAnswers(answers);

            return QuestionResult.success(question, aiConfig.getAiType().getName());
        } catch (Exception e) {
            log.error("AI答题失败: {}", e.getMessage(), e);
            return QuestionResult.error(question, "AI答题失败: " + e.getMessage());
        }
    }

    private QuestionResult answerWithRandom(Question question) {
        Random random = new Random();
        List<String> options = question.getOptions();
        
        if (options == null || options.isEmpty()) {
            return QuestionResult.error(question, "无选项可随机选择");
        }

        QuestionTypeEnum typeEnum = QuestionTypeEnum.fromName(question.getType());
        List<String> answers = new java.util.ArrayList<>();

        switch (typeEnum) {
            case SINGLE_CHOICE:
                int index = random.nextInt(options.size());
                answers.add(options.get(index));
                break;
            case MULTIPLE_CHOICE:
                int count = random.nextInt(options.size()) + 1;
                for (int i = 0; i < count; i++) {
                    int idx = random.nextInt(options.size());
                    if (!answers.contains(options.get(idx))) {
                        answers.add(options.get(idx));
                    }
                }
                break;
            case TRUE_OR_FALSE:
                answers.add(random.nextBoolean() ? "正确" : "错误");
                break;
            default:
                return QuestionResult.notFound(question);
        }

        question.setAnswers(answers);
        return QuestionResult.success(question, "随机答案");
    }

    private String formatAnswer(YinghuaExamApi.ExamTopic topic) {
        List<String> answers = topic.getAnswers();
        if (answers == null || answers.isEmpty()) {
            return "";
        }

        QuestionTypeEnum typeEnum = QuestionTypeEnum.fromName(convertQuestionType(topic.getType()));
        
        switch (typeEnum) {
            case SINGLE_CHOICE:
                return "[" + answers.get(0) + "]";
            case MULTIPLE_CHOICE:
                return "[" + String.join(" ", answers) + "]";
            case TRUE_OR_FALSE:
                return "[" + answers.get(0) + "]";
            default:
                return JSONArray.toJSONString(answers);
        }
    }
}
