package com.studyproxy.api.yinghua;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.config.YinghuaConfig;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.util.HttpUtil;
import com.studyproxy.util.JsonUtil;
import com.studyproxy.util.UaUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class YinghuaLoginApi {

    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 150;

    @Resource
    private YinghuaConfig yinghuaConfig;

    @Resource
    private HttpUtil httpUtil;

    @Resource
    private OkHttpClient okHttpClient;

    public String login(YinghuaUserCache userCache) {
        return executeWithRetry(() -> doLogin(userCache), "login");
    }

    private String doLogin(YinghuaUserCache userCache) {
        String url = userCache.getPreUrl() + "/user/login.json";

        RequestBody formBody = new FormBody.Builder()
                .add("username", userCache.getAccount())
                .add("password", userCache.getPassword())
                .add("code", userCache.getVerCode() != null ? userCache.getVerCode() : "")
                .add("redirect", userCache.getPreUrl())
                .build();

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Origin", userCache.getPreUrl());
        headers.put("Referer", userCache.getPreUrl() + "/user/login");

        CookieJar cookieJar = userCache.getCookieJar();
        OkHttpClient client = cookieJar != null 
            ? okHttpClient.newBuilder().cookieJar(cookieJar).build() 
            : okHttpClient;

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(formBody);
        
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("登录请求失败: " + response.code());
            }
            
            ResponseBody body = response.body();
            if (body != null) {
                String responseStr = body.string();
                log.info("[登录响应] {}", responseStr);
                
                validateResponse(responseStr);

                JSONObject jsonResponse = JsonUtil.parseObject(responseStr);
                if (jsonResponse.getBoolean("status") != null && jsonResponse.getBoolean("status")) {
                    String redirect = jsonResponse.getString("redirect");
                    if (redirect != null) {
                        String token = extractParam(redirect, "token");
                        String sign = extractParam(redirect, "sign");
                        userCache.setToken(token);
                        userCache.setSign(sign);
                        return responseStr;
                    }
                }

                String msg = jsonResponse.getString("msg");
                if (msg != null) {
                    log.warn("[登录失败] msg={}", msg);
                    throw new RuntimeException(msg);
                }

                throw new RuntimeException("登录失败");
            }
            
            throw new RuntimeException("登录响应体为空");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("登录请求失败: " + e.getMessage(), e);
        }
    }

    public byte[] getVerificationCodeImage(YinghuaUserCache userCache) {
        return executeWithRetryBytes(() -> doGetVerificationCodeImage(userCache), "getVerificationCodeImage");
    }

    private byte[] doGetVerificationCodeImage(YinghuaUserCache userCache) {
        String r = String.valueOf(Math.random());
        String url = userCache.getPreUrl() + "/service/code?r=" + r;

        Request.Builder builder = new Request.Builder().url(url).get();
        builder.addHeader("User-Agent", UaUtil.getDefaultUA());
        builder.addHeader("Connection", "keep-alive");

        CookieJar cookieJar = userCache.getCookieJar();
        OkHttpClient client = cookieJar != null 
            ? okHttpClient.newBuilder().cookieJar(cookieJar).build() 
            : okHttpClient;

        try (Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取验证码失败: " + response.code());
            }
            
            ResponseBody body = response.body();
            if (body != null) {
                return body.bytes();
            }
            throw new IOException("验证码响应体为空");
        } catch (IOException e) {
            throw new RuntimeException("获取验证码图片失败", e);
        }
    }

    public String getVerificationCode(YinghuaUserCache userCache) {
        byte[] imageBytes = getVerificationCodeImage(userCache);
        return new String(imageBytes);
    }

    public String keepAlive(YinghuaUserCache userCache) {
        return executeWithRetry(() -> doKeepAlive(userCache), "keepAlive");
    }

    private String doKeepAlive(YinghuaUserCache userCache) {
        String url = userCache.getPreUrl() + "/api/online.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        return response;
    }

    private void validateResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("服务器返回数据为空");
        }
        if (response.contains("502 Bad Gateway") || response.contains("504 Gateway Time-out")) {
            throw new RuntimeException("服务器网关错误");
        }
        if (response.contains("\"status\":false,\"_code\":500")) {
            throw new RuntimeException("服务器内部错误");
        }
    }

    private String extractParam(String url, String paramName) {
        if (url == null) {
            return null;
        }
        String[] params = url.split("&");
        for (String param : params) {
            if (param.contains(paramName + "=")) {
                return param.split("=")[1];
            }
        }
        return null;
    }

    private String executeWithRetry(Supplier<String> action, String actionName) {
        Exception lastError = null;
        for (int i = DEFAULT_RETRY_COUNT; i >= 0; i--) {
            try {
                return action.get();
            } catch (Exception e) {
                lastError = e;
                log.warn("[{}] 请求失败，剩余重试次数: {}, 错误: {}", actionName, i, e.getMessage());
                if (i > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new RuntimeException("[" + actionName + "] 重试" + DEFAULT_RETRY_COUNT + "次后仍然失败", lastError);
    }

    private byte[] executeWithRetryBytes(Supplier<byte[]> action, String actionName) {
        Exception lastError = null;
        for (int i = DEFAULT_RETRY_COUNT; i >= 0; i--) {
            try {
                return action.get();
            } catch (Exception e) {
                lastError = e;
                log.warn("[{}] 请求失败，剩余重试次数: {}, 错误: {}", actionName, i, e.getMessage());
                if (i > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new RuntimeException("[" + actionName + "] 重试" + DEFAULT_RETRY_COUNT + "次后仍然失败", lastError);
    }
}
