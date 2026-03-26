package com.studyproxy.api.yinghua;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.config.YinghuaConfig;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.util.HttpUtil;
import com.studyproxy.util.UaUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class YinghuaExamApi {

    @Resource
    private YinghuaConfig yinghuaConfig;

    @Resource
    private HttpUtil httpUtil;

    public String startExam(YinghuaUserCache userCache, String courseId, String nodeId, String examId) {
        String url = userCache.getPreUrl() + "/api/exam/start.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("courseId", courseId);
        formData.put("nodeId", nodeId);
        formData.put("examId", examId);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        return httpUtil.postMultipart(url, formData, headers);
    }

    public String getExamTopics(YinghuaUserCache userCache, String nodeId, String examId) {
        String url = userCache.getPreUrl() + "/api/exam/paper.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("nodeId", nodeId);
        formData.put("examId", examId);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        return httpUtil.postMultipart(url, formData, headers);
    }

    public String submitExamAnswer(YinghuaUserCache userCache, String examId, String answerId, 
                                  String questionContent, String answer, String isFinish) {
        String url = userCache.getPreUrl() + "/api/exam/answer.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("examId", examId);
        formData.put("answerId", answerId);
        formData.put("question", questionContent);
        formData.put("answer", answer);
        formData.put("isFinish", isFinish);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        return httpUtil.postMultipart(url, formData, headers);
    }

    public String getWorkTopics(YinghuaUserCache userCache, String nodeId, String workId) {
        String url = userCache.getPreUrl() + "/api/work/paper.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("nodeId", nodeId);
        formData.put("workId", workId);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        return httpUtil.postMultipart(url, formData, headers);
    }

    public String submitWorkAnswer(YinghuaUserCache userCache, String workId, String answerId,
                                  String questionContent, String answer) {
        String url = userCache.getPreUrl() + "/api/work/answer.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("workId", workId);
        formData.put("answerId", answerId);
        formData.put("question", questionContent);
        formData.put("answer", answer);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        return httpUtil.postMultipart(url, formData, headers);
    }

    public List<ExamTopic> parseExamTopics(String html) {
        List<ExamTopic> topics = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements questionElements = doc.select(".TiMu");

        for (Element questionElement : questionElements) {
            ExamTopic topic = new ExamTopic();

            Element typeElement = questionElement.selectFirst(".NewZti");
            if (typeElement != null) {
                topic.setType(typeElement.text().trim());
            }

            Element contentElement = questionElement.selectFirst(".Zti");
            if (contentElement != null) {
                topic.setContent(contentElement.text().trim());
            }

            Element answerIdElement = questionElement.selectFirst("input[name=answerId]");
            if (answerIdElement != null) {
                topic.setAnswerId(answerIdElement.attr("value"));
            }

            Elements optionElements = questionElement.select(".Xzti");
            List<String> options = new ArrayList<>();
            for (Element optionElement : optionElements) {
                String optionText = optionElement.text().trim();
                options.add(optionText);
            }
            topic.setOptions(options);

            topics.add(topic);
        }

        return topics;
    }

    public static class ExamTopic {
        private String type;
        private String content;
        private String answerId;
        private List<String> options;
        private List<String> answers;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAnswerId() {
            return answerId;
        }

        public void setAnswerId(String answerId) {
            this.answerId = answerId;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public void setAnswers(List<String> answers) {
            this.answers = answers;
        }
    }
}
