package com.studyproxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "studyproxy.yinghua")
public class YinghuaConfig {

    private String platform = "Android";

    private String version = "1.4.8";

    private String preUrl = "https://yinghua.example.com";
}