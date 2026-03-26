package com.studyproxy.api.yinghua;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.config.YinghuaConfig;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.util.HttpUtil;
import com.studyproxy.util.JsonUtil;
import com.studyproxy.util.UaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class YinghuaStudyApi {

    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 150;

    @Resource
    private YinghuaConfig yinghuaConfig;

    @Resource
    private HttpUtil httpUtil;

    public String submitStudyTime(YinghuaUserCache userCache, String nodeId, String studyId, Integer studyTime) {
        return executeWithRetry(() -> doSubmitStudyTime(userCache, nodeId, studyId, studyTime), "submitStudyTime");
    }

    public String submitStudyTimeWithCaptcha(YinghuaUserCache userCache, String nodeId, String studyId,
                                             Integer studyTime, String captcha) {
        return doSubmitStudyTimeWithCaptcha(userCache, nodeId, studyId, studyTime, captcha);
    }

    private String doSubmitStudyTime(YinghuaUserCache userCache, String nodeId, String studyId, Integer studyTime) {
        String url = userCache.getPreUrl() + "/api/node/study.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("nodeId", nodeId);
        formData.put("token", userCache.getToken());
        formData.put("terminal", "Android");
        formData.put("studyTime", String.valueOf(studyTime));
        formData.put("studyId", studyId);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        return response;
    }

    private String doSubmitStudyTimeWithCaptcha(YinghuaUserCache userCache, String nodeId, String studyId,
                                                 Integer studyTime, String captcha) {
        String url = userCache.getPreUrl() + "/api/node/study.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("nodeId", nodeId);
        formData.put("token", userCache.getToken());
        formData.put("terminal", "Android");
        formData.put("studyTime", String.valueOf(studyTime));
        formData.put("studyId", studyId);
        formData.put("code", captcha);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        return response;
    }

    public byte[] getCaptchaImage(YinghuaUserCache userCache) {
        try {
            String url = userCache.getPreUrl() + "/service/code?r=" + Math.random();

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            if (userCache.getCookieJar() != null) {
                return httpUtil.getBytes(url, headers, userCache.getCookieJar());
            } else {
                return httpUtil.getBytes(url, headers);
            }
        } catch (Exception e) {
            log.error("[{}] 获取验证码图片失败: {}", userCache.getAccount(), e.getMessage());
            return null;
        }
    }

    public String openCoursePage(YinghuaUserCache userCache, String courseId, String chapterId, String nodeId) {
        try {
            String url = userCache.getPreUrl() + "/user/node?courseId=" + courseId + "&chapterId=" + chapterId + "&nodeId=" + nodeId;

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            String response;
            if (userCache.getCookieJar() != null) {
                response = httpUtil.get(url, headers, userCache.getCookieJar());
            } else {
                response = httpUtil.get(url, headers);
            }

            log.debug("[{}] 打开课程页面响应: {}", userCache.getAccount(), response);
            return response;
        } catch (Exception e) {
            log.error("[{}] 打开课程页面失败: {}", userCache.getAccount(), e.getMessage());
            return null;
        }
    }

    public String submitCaptchaToStart(YinghuaUserCache userCache, String courseId, String chapterId, String nodeId, String captcha) {
        try {
            String url = userCache.getPreUrl() + "/user/node/play.json";

            Map<String, String> formData = new HashMap<>();
            formData.put("courseId", courseId);
            formData.put("chapterId", chapterId);
            formData.put("nodeId", nodeId);
            formData.put("code", captcha);

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.put("X-Requested-With", "XMLHttpRequest");
            headers.put("Referer", userCache.getPreUrl() + "/user/node?courseId=" + courseId + "&chapterId=" + chapterId + "&nodeId=" + nodeId);

            String response;
            if (userCache.getCookieJar() != null) {
                response = httpUtil.postMultipart(url, formData, headers, userCache.getCookieJar());
            } else {
                response = httpUtil.postMultipart(url, formData, headers);
            }

            log.debug("[{}] 提交验证码开始播放响应: {}", userCache.getAccount(), response);
            return response;
        } catch (Exception e) {
            log.error("[{}] 提交验证码开始播放失败: {}", userCache.getAccount(), e.getMessage());
            return null;
        }
    }

    public String getVideoStudyTime(YinghuaUserCache userCache, String nodeId) {
        return executeWithRetry(() -> doGetVideoStudyTime(userCache, nodeId), "getVideoStudyTime");
    }

    private String doGetVideoStudyTime(YinghuaUserCache userCache, String nodeId) {
        String url = userCache.getPreUrl() + "/api/node/video.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("nodeId", nodeId);
        formData.put("token", userCache.getToken());

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        return response;
    }

    public String getVideoWatchRecord(YinghuaUserCache userCache, String courseId, int page) {
        return executeWithRetry(() -> doGetVideoWatchRecord(userCache, courseId, page), "getVideoWatchRecord");
    }

    private String doGetVideoWatchRecord(YinghuaUserCache userCache, String courseId, int page) {
        String url = userCache.getPreUrl() + "/api/record/video.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("courseId", courseId);
        formData.put("page", String.valueOf(page));

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        return response;
    }

    public String getVideoWatchRecordPC(YinghuaUserCache userCache, String courseId, int page) {
        return executeWithRetry(() -> doGetVideoWatchRecordPC(userCache, courseId, page), "getVideoWatchRecordPC");
    }

    private String doGetVideoWatchRecordPC(YinghuaUserCache userCache, String courseId, int page) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String url = userCache.getPreUrl() + "/user/study_record/video.json?courseId=" + courseId + "&_=" + timestamp + "&page=" + page;

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.get(url, headers);

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
}
