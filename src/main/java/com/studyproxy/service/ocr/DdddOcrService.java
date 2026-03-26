package com.studyproxy.service.ocr;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DdddOcrService {

    @Value("${ocr.service.url:http://localhost:5000}")
    private String ocrServiceUrl;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .build();
        log.info("DdddOcrService initialized, url: {}", ocrServiceUrl);
    }

    public String recognize(byte[] imageData) {
        log.info("OCR recognize called, image size: {} bytes", imageData.length);
        
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        String jsonBody = "{\"image\":\"" + base64Image + "\"}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(ocrServiceUrl + "/ocr")
                .post(body)
                .build();

        log.info("Sending OCR request to: {}", ocrServiceUrl + "/ocr");
        
        try (Response response = httpClient.newCall(request).execute()) {
            log.info("OCR response received, code: {}", response.code());
            
            if (!response.isSuccessful()) {
                log.error("OCR service returned error: {}", response.code());
                throw new RuntimeException("OCR service error: " + response.code());
            }

            String responseBody = response.body().string();
            log.info("OCR response body: {}", responseBody);
            
            JSONObject result = JSONObject.parseObject(responseBody);
            
            if (result.containsKey("error")) {
                log.error("OCR recognition error: {}", result.getString("error"));
                throw new RuntimeException("OCR error: " + result.getString("error"));
            }

            String text = result.getString("text");
            log.info("OCR result: {}", text);
            return text != null ? text.trim() : "";
            
        } catch (IOException e) {
            log.error("OCR service call failed: {}", e.getMessage());
            throw new RuntimeException("OCR service call failed: " + e.getMessage(), e);
        }
    }

    public String recognizeFromUrl(String imageUrl) {
        String jsonBody = "{\"url\":\"" + imageUrl + "\"}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(ocrServiceUrl + "/ocr/url")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("OCR service returned error: {}", response.code());
                throw new RuntimeException("OCR service error: " + response.code());
            }

            String responseBody = response.body().string();
            JSONObject result = JSONObject.parseObject(responseBody);
            
            if (result.containsKey("error")) {
                log.error("OCR recognition error: {}", result.getString("error"));
                throw new RuntimeException("OCR error: " + result.getString("error"));
            }

            String text = result.getString("text");
            log.info("OCR result from URL: {}", text);
            return text != null ? text.trim() : "";
            
        } catch (IOException e) {
            log.error("OCR service call failed: {}", e.getMessage());
            throw new RuntimeException("OCR service call failed: " + e.getMessage(), e);
        }
    }

    public boolean isHealthy() {
        Request request = new Request.Builder()
                .url(ocrServiceUrl + "/health")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
}
