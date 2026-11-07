package com.studyproxy.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "accounts")
public class AccountConfig {

    private List<YinghuaAccount> yinghua;
    private Settings settings = new Settings();

    @Data
    public static class YinghuaAccount {
        private String account;
        private String password;
        private String preUrl;
    }

    @Data
    public static class Settings {
        private boolean autoStart = true;
        private int interval = 30;
    }
}
