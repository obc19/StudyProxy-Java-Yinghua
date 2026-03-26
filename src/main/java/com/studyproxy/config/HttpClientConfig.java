package com.studyproxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "studyproxy.http")
public class HttpClientConfig {

    private int connectTimeout = 30000;

    private int readTimeout = 30000;

    private int writeTimeout = 30000;

    private int retryTimes = 3;
}