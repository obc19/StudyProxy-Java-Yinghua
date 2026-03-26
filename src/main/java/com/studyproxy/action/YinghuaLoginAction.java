package com.studyproxy.action;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaLoginApi;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.service.ocr.DdddOcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class YinghuaLoginAction {

    private static final int MAX_RETRY = 10;

    @Resource
    private YinghuaLoginApi yinghuaLoginApi;

    @Resource
    private DdddOcrService ddddOcrService;

    public String login(YinghuaUserCache userCache) {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                log.info("[{}] 第{}次尝试登录 - 获取验证码", userCache.getAccount(), i + 1);
                
                byte[] captchaImage = yinghuaLoginApi.getVerificationCodeImage(userCache);
                log.info("[{}] 验证码图片大小: {} bytes", userCache.getAccount(), captchaImage.length);
                
                String verCode = ddddOcrService.recognize(captchaImage);
                log.info("[{}] 验证码识别结果: '{}'", userCache.getAccount(), verCode);
                
                if (verCode == null || verCode.length() != 4) {
                    log.warn("[{}] 验证码长度不对，重试", userCache.getAccount());
                    continue;
                }
                
                userCache.setVerCode(verCode);
                
                String jsonStr = yinghuaLoginApi.login(userCache);
                log.info("[{}] 登录响应: {}", userCache.getAccount(), jsonStr);
                
                if (jsonStr.contains("验证码有误") || jsonStr.contains("验证码错误")) {
                    log.warn("[{}] 验证码识别错误，重试", userCache.getAccount());
                    continue;
                }
                
                if (isPasswordError(jsonStr)) {
                    log.error("[{}] 账号或密码错误，程序退出！", userCache.getAccount());
                    System.exit(1);
                }
                
                JSONObject jsonResponse = JSONObject.parseObject(jsonStr);
                if (jsonResponse.getBoolean("status") != null && jsonResponse.getBoolean("status")) {
                    String redirect = jsonResponse.getString("redirect");
                    if (redirect != null) {
                        String token = extractParam(redirect, "token");
                        String sign = extractParam(redirect, "sign");
                        userCache.setToken(token);
                        userCache.setSign(sign);
                    }
                    log.info("[{}] 登录成功", userCache.getAccount());
                    return jsonStr;
                }
                
                String msg = jsonResponse.getString("msg");
                if (msg != null && !msg.contains("验证码")) {
                    log.warn("[{}] 登录失败: {}", userCache.getAccount(), msg);
                }
                
            } catch (Exception e) {
                log.error("[{}] 第{}次登录失败: {}", userCache.getAccount(), i + 1, e.getMessage());
            }
        }
        
        throw new RuntimeException("登录失败，验证码识别多次失败");
    }

    private boolean isPasswordError(String response) {
        if (response == null) {
            return false;
        }
        String lowerResponse = response.toLowerCase();
        return lowerResponse.contains("密码错误") 
            || lowerResponse.contains("用户名或密码错误")
            || lowerResponse.contains("账号不存在")
            || lowerResponse.contains("用户不存在")
            || lowerResponse.contains("账号或密码错误")
            || lowerResponse.contains("用户名错误")
            || lowerResponse.contains("密码不正确")
            || (response.contains("\"status\":false") && response.contains("密码"));
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
