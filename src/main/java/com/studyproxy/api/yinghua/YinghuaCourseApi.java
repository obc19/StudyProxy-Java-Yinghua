package com.studyproxy.api.yinghua;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.config.YinghuaConfig;
import com.studyproxy.entity.yinghua.YinghuaCourse;
import com.studyproxy.entity.yinghua.YinghuaNode;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.util.HttpUtil;
import com.studyproxy.util.JsonUtil;
import com.studyproxy.util.UaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class YinghuaCourseApi {

    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 150;

    @Resource
    private YinghuaConfig yinghuaConfig;

    @Resource
    private HttpUtil httpUtil;

    public List<YinghuaCourse> getCourseList(YinghuaUserCache userCache) {
        return executeWithRetryList(() -> doGetCourseList(userCache), "getCourseList");
    }

    private List<YinghuaCourse> doGetCourseList(YinghuaUserCache userCache) {
        String url = userCache.getPreUrl() + "/api/course/list.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("type", "0");
        formData.put("token", userCache.getToken());

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response;
        if (userCache.getCookieJar() != null) {
            response = httpUtil.postMultipart(url, formData, headers, userCache.getCookieJar());
        } else {
            response = httpUtil.postMultipart(url, formData, headers);
        }

        log.debug("[{}] 课程列表响应: {}", userCache.getAccount(), response);

        validateResponse(response);

        JSONObject jsonResponse = JsonUtil.parseObject(response);
        if (!"获取数据成功".equals(jsonResponse.getString("msg"))) {
            throw new RuntimeException("获取课程列表失败: " + response);
        }

        JSONObject result = jsonResponse.getJSONObject("result");
        JSONArray list = result.getJSONArray("list");

        List<YinghuaCourse> courseList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                JSONObject item = list.getJSONObject(i);
                YinghuaCourse course = new YinghuaCourse();
                course.setId(String.valueOf(item.getInteger("id")));
                course.setName(item.getString("name"));
                course.setMode(item.getInteger("mode"));
                course.setProgress(item.getDouble("progress"));
                course.setVideoCount(item.getInteger("videoCount"));
                course.setVideoLearned(item.getInteger("videoLearned"));

                try {
                    course.setStartDate(sdf.parse(item.getString("startDate")));
                    course.setEndDate(sdf.parse(item.getString("endDate")));
                } catch (Exception e) {
                    log.error("日期解析失败", e);
                }

                courseList.add(course);
            }
        }

        return courseList;
    }

    public YinghuaCourse getCourseDetail(YinghuaUserCache userCache, String courseId) {
        return executeWithRetryCourse(() -> doGetCourseDetail(userCache, courseId), "getCourseDetail");
    }

    private YinghuaCourse doGetCourseDetail(YinghuaUserCache userCache, String courseId) {
        String url = userCache.getPreUrl() + "/api/course/detail.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("courseId", courseId);
        formData.put("token", userCache.getToken());

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        JSONObject jsonResponse = JsonUtil.parseObject(response);
        if (!"获取数据成功".equals(jsonResponse.getString("msg"))) {
            throw new RuntimeException("获取课程详情失败: " + response);
        }

        JSONObject result = jsonResponse.getJSONObject("result");
        JSONObject data = result.getJSONObject("data");

        YinghuaCourse course = new YinghuaCourse();
        course.setId(String.valueOf(data.getInteger("id")));
        course.setName(data.getString("name"));
        course.setMode(data.getInteger("mode"));
        course.setProgress(data.getDouble("progress"));
        course.setVideoCount(data.getInteger("videoCount"));
        course.setVideoLearned(data.getInteger("videoLearned"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            course.setStartDate(sdf.parse(data.getString("startDate")));
            course.setEndDate(sdf.parse(data.getString("endDate")));
        } catch (Exception e) {
            log.error("日期解析失败", e);
        }

        return course;
    }

    public List<YinghuaNode> getVideoList(YinghuaUserCache userCache, String courseId) {
        return executeWithRetryNodeList(() -> doGetVideoList(userCache, courseId), "getVideoList");
    }

    private List<YinghuaNode> doGetVideoList(YinghuaUserCache userCache, String courseId) {
        String url = userCache.getPreUrl() + "/api/course/chapter.json";

        Map<String, String> formData = new HashMap<>();
        formData.put("platform", yinghuaConfig.getPlatform());
        formData.put("version", yinghuaConfig.getVersion());
        formData.put("token", userCache.getToken());
        formData.put("courseId", courseId);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", UaUtil.getDefaultUA());

        String response = httpUtil.postMultipart(url, formData, headers);

        validateResponse(response);

        JSONObject jsonResponse = JsonUtil.parseObject(response);
        if (!"获取数据成功".equals(jsonResponse.getString("msg"))) {
            throw new RuntimeException("获取视频列表失败: " + response);
        }

        JSONObject result = jsonResponse.getJSONObject("result");
        JSONArray list = result.getJSONArray("list");

        List<YinghuaNode> nodeList = new ArrayList<>();

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                JSONObject chapter = list.getJSONObject(i);
                String chapterId = String.valueOf(chapter.getInteger("id"));
                JSONArray nodeListJson = chapter.getJSONArray("nodeList");

                if (nodeListJson != null) {
                    for (int j = 0; j < nodeListJson.size(); j++) {
                        JSONObject node = nodeListJson.getJSONObject(j);
                        YinghuaNode yinghuaNode = new YinghuaNode();
                        yinghuaNode.setId(String.valueOf(node.getInteger("id")));
                        yinghuaNode.setCourseId(courseId);
                        yinghuaNode.setChapterId(chapterId);
                        yinghuaNode.setName(node.getString("name"));
                        yinghuaNode.setVideoDuration(Integer.parseInt(node.getString("videoDuration")));
                        yinghuaNode.setNodeLock(node.getInteger("nodeLock"));
                        yinghuaNode.setTabVideo(node.getBoolean("tabVideo"));
                        yinghuaNode.setTabFile(node.getBoolean("tabFile"));
                        yinghuaNode.setTabExam(node.getBoolean("tabExam"));
                        yinghuaNode.setTabWork(node.getBoolean("tabWork"));

                        nodeList.add(yinghuaNode);
                    }
                }
            }
        }

        return nodeList;
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

    private <T> T executeWithRetryGeneric(Supplier<T> action, String actionName) {
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

    private List<YinghuaCourse> executeWithRetryList(Supplier<List<YinghuaCourse>> action, String actionName) {
        return executeWithRetryGeneric(action, actionName);
    }

    private YinghuaCourse executeWithRetryCourse(Supplier<YinghuaCourse> action, String actionName) {
        return executeWithRetryGeneric(action, actionName);
    }

    private List<YinghuaNode> executeWithRetryNodeList(Supplier<List<YinghuaNode>> action, String actionName) {
        return executeWithRetryGeneric(action, actionName);
    }
}
