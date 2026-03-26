package com.studyproxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "")
public class StudyProxyConfig {

    private Setting setting;

    @Data
    public static class Setting {
        private BasicSetting basicSetting;
        private EmailInform emailInform;
        private AiSetting aiSetting;
        private ApiQueSetting apiQueSetting;
    }

    @Data
    public static class BasicSetting {
        private Integer completionTone = 1;
        private Integer colorLog = 1;
        private Integer logOutFileSw = 1;
        private String logLevel = "INFO";
        private Integer logModel = 0;
    }

    @Data
    public static class EmailInform {
        private Integer sw = 0;
        private String SMTPHost = "";
        private String SMTPPort = "";
        private String userName = "";
        private String password = "";
    }

    @Data
    public static class AiSetting {
        private String aiType = "TONGYI";
        private String aiUrl = "";
        private String model = "";
        private String API_KEY = "";
    }

    @Data
    public static class ApiQueSetting {
        private String url = "http://localhost:8083";
    }
}