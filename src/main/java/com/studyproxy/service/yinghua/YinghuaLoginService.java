package com.studyproxy.service.yinghua;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaLoginApi;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.service.ocr.DdddOcrService;
import com.studyproxy.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class YinghuaLoginService {

    private static final int MAX_RETRY_COUNT = 10;
    private static final long CAPTCHA_RETRY_DELAY_MS = 500;
    private static final long LOGIN_FAIL_DELAY_MS = 5000;

    @Resource
    private YinghuaLoginApi yinghuaLoginApi;

    @Resource
    private DdddOcrService ddddOcrService;

    public String login(YinghuaUserCache userCache) {
        log.info("[{}] 开始登录", userCache.getAccount());

        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            try {
                log.info("[{}] 第{}次尝试 - 获取验证码", userCache.getAccount(), i + 1);
                
                byte[] captchaImage = yinghuaLoginApi.getVerificationCodeImage(userCache);
                log.info("[{}] 验证码图片大小：{} bytes", userCache.getAccount(), captchaImage.length);
                
                String verCode = ddddOcrService.recognize(captchaImage);
                log.info("[{}] 验证码识别结果：'{}'", userCache.getAccount(), verCode);
                
                if (verCode == null || verCode.length() != 4) {
                    log.warn("[{}] 验证码长度不对，重试", userCache.getAccount());
                    sleep(CAPTCHA_RETRY_DELAY_MS);
                    continue;
                }
                
                userCache.setVerCode(verCode);
                
                String response = yinghuaLoginApi.login(userCache);
                log.info("[{}] 登录响应：{}", userCache.getAccount(), response);
                
                if (response.contains("验证码有误")) {
                    log.warn("[{}] 验证码识别错误，重试", userCache.getAccount());
                    sleep(CAPTCHA_RETRY_DELAY_MS);
                    continue;
                }

                if (response.contains("密码错误") || response.contains("用户名或密码")) {
                    log.error("[{}] 密码错误，等待 {} 秒后重试", userCache.getAccount(), LOGIN_FAIL_DELAY_MS / 1000);
                    sleep(LOGIN_FAIL_DELAY_MS);
                    continue;
                }
                
                JSONObject jsonResponse = JsonUtil.parseObject(response);
                if (jsonResponse.getBoolean("status") != null && jsonResponse.getBoolean("status")) {
                    String redirect = jsonResponse.getString("redirect");
                    if (redirect != null) {
                        String token = extractParam(redirect, "token");
                        String sign = extractParam(redirect, "sign");
                        userCache.setToken(token);
                        userCache.setSign(sign);
                        log.info("[{}] 登录成功", userCache.getAccount());
                        return response;
                    }
                }
                
                String msg = jsonResponse.getString("msg");
                if (msg != null && !msg.contains("验证码")) {
                    log.warn("[{}] 登录失败：{}，等待 {} 秒后重试", userCache.getAccount(), msg, LOGIN_FAIL_DELAY_MS / 1000);
                    sleep(LOGIN_FAIL_DELAY_MS);
                    continue;
                }
                
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("验证码")) {
                    sleep(CAPTCHA_RETRY_DELAY_MS);
                    continue;
                }
                if (errorMsg != null && (errorMsg.contains("密码") || errorMsg.contains("用户名"))) {
                    log.error("[{}] 登录失败：{}，等待 {} 秒后重试", userCache.getAccount(), errorMsg, LOGIN_FAIL_DELAY_MS / 1000);
                    sleep(LOGIN_FAIL_DELAY_MS);
                    continue;
                }
                if (i == MAX_RETRY_COUNT - 1) {
                    throw new RuntimeException("登录失败：" + errorMsg, e);
                }
                log.warn("[{}] 第{}次尝试失败：{}", userCache.getAccount(), i + 1, errorMsg);
                sleep(LOGIN_FAIL_DELAY_MS);
            }
        }
        
        throw new RuntimeException("登录失败，验证码识别多次失败");
    }

    public String loginWithVerCode(YinghuaUserCache userCache) {
        log.info("[{}] 开始登录（手动验证码）", userCache.getAccount());

        if (userCache.getVerCode() == null || userCache.getVerCode().isEmpty()) {
            throw new RuntimeException("验证码不能为空，请先获取验证码并设置");
        }

        String response = yinghuaLoginApi.login(userCache);
        log.info("[{}] 登录响应：{}", userCache.getAccount(), response);

        JSONObject jsonResponse = JsonUtil.parseObject(response);
        if (jsonResponse.getBoolean("status") != null && jsonResponse.getBoolean("status")) {
            String redirect = jsonResponse.getString("redirect");
            if (redirect != null) {
                String token = extractParam(redirect, "token");
                String sign = extractParam(redirect, "sign");
                userCache.setToken(token);
                userCache.setSign(sign);
                log.info("[{}] 登录成功", userCache.getAccount());
                return response;
            }
        }

        String msg = jsonResponse.getString("msg");
        throw new RuntimeException(msg != null ? msg : "登录失败");
    }

    public byte[] getVerificationCodeImage(YinghuaUserCache userCache) {
        return yinghuaLoginApi.getVerificationCodeImage(userCache);
    }

    public String keepAlive(YinghuaUserCache userCache) {
        return yinghuaLoginApi.keepAlive(userCache);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
}
