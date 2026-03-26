package com.studyproxy.config;

import com.studyproxy.config.HttpClientConfig;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpConfig {

    @Resource
    private HttpClientConfig httpClientConfig;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(httpClientConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(httpClientConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(httpClientConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(true)
                .build();
    }
}