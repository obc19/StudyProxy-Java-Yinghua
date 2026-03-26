package com.studyproxy.service.yinghua;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaCourseApi;
import com.studyproxy.api.yinghua.YinghuaLoginApi;
import com.studyproxy.api.yinghua.YinghuaStudyApi;
import com.studyproxy.entity.AccountConfig;
import com.studyproxy.entity.ai.AiConfig;
import com.studyproxy.entity.yinghua.YinghuaCourse;
import com.studyproxy.entity.yinghua.YinghuaNode;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.exception.CourseNotStartedException;
import com.studyproxy.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class YinghuaVideoStudyService {

    private static final int SUBMIT_INTERVAL_SECONDS = 5;
    private static final int HEARTBEAT_INTERVAL_SECONDS = 300;

    @Resource
    private YinghuaCourseApi yinghuaCourseApi;

    @Resource
    private YinghuaLoginApi yinghuaLoginApi;

    @Resource
    private YinghuaStudyApi yinghuaStudyApi;

    @Resource
    private YinghuaAiService yinghuaAiService;

    @Resource
    private AccountConfig accountConfig;

    public void studyAllCourses(YinghuaUserCache userCache) {
        log.info("[{}] ========== 开始刷课 ==========", userCache.getAccount());

        List<YinghuaCourse> courseList = yinghuaCourseApi.getCourseList(userCache);
        log.info("[{}] 共获取到 {} 门课程", userCache.getAccount(), courseList.size());

        for (int i = 0; i < courseList.size(); i++) {
            YinghuaCourse course = courseList.get(i);
            log.info("[{}] ========== 课程 {}/{}: {} (进度: {}%) ==========",
                    userCache.getAccount(), i + 1, courseList.size(),
                    course.getName(), String.format("%.1f", course.getProgress()));

            try {
                studyCourse(userCache, course);
            } catch (Exception e) {
                log.error("[{}] 课程 {} 刷课失败: {}", userCache.getAccount(), course.getName(), e.getMessage(), e);
            }
        }

        log.info("[{}] ========== 刷课完成 ==========", userCache.getAccount());
    }

    public void studyCourse(YinghuaUserCache userCache, YinghuaCourse course) {
        List<YinghuaNode> nodeList = getFullVideoList(userCache, course.getId());
        log.info("[{}] 课程 [{}] 共 {} 个节点", userCache.getAccount(), course.getName(), nodeList.size());

        if (nodeList.isEmpty()) {
            log.warn("[{}] 课程 [{}] 没有找到任何节点", userCache.getAccount(), course.getName());
            return;
        }

        int videoCompleted = 0;
        int videoTotal = 0;
        int examCompleted = 0;
        int examTotal = 0;
        int workCompleted = 0;
        int workTotal = 0;

        AiConfig aiConfig = getAiConfig();

        log.info("[{}] 开始遍历 {} 个节点...", userCache.getAccount(), nodeList.size());
        
        for (int i = 0; i < nodeList.size(); i++) {
            YinghuaNode node = nodeList.get(i);
            
            if (node.getTabVideo() != null && node.getTabVideo()) {
                videoTotal++;
                try {
                    if (studyVideo(userCache, node)) {
                        videoCompleted++;
                    }
                } catch (CourseNotStartedException e) {
                    log.warn("[{}] 课程 [{}] 未到解锁时间已自动跳过", userCache.getAccount(), course.getName());
                    return;
                } catch (Exception e) {
                    log.error("[{}] 视频 [{}] 学习失败: {}", userCache.getAccount(), node.getName(), e.getMessage());
                }
            }

            if (node.getTabExam() != null && node.getTabExam()) {
                examTotal++;
                if (handleExam(userCache, course.getId(), node, aiConfig)) {
                    examCompleted++;
                }
            }

            if (node.getTabWork() != null && node.getTabWork()) {
                workTotal++;
                if (handleWork(userCache, course.getId(), node, aiConfig)) {
                    workCompleted++;
                }
            }
        }

        log.info("[{}] 课程 [{}] 完成 - 视频:{}/{} 考试:{}/{} 作业:{}/{}",
                userCache.getAccount(), course.getName(),
                videoCompleted, videoTotal, examCompleted, examTotal, workCompleted, workTotal);
    }

    private AiConfig getAiConfig() {
        AccountConfig.Settings settings = accountConfig.getSettings();
        if (settings != null && settings.isAutoAnswer()) {
            AccountConfig.AiConfig configAiConfig = settings.getAiConfig();
            AiConfig aiConfig = new AiConfig();
            aiConfig.setAiType(AiConfig.AiTypeEnum.fromCode(configAiConfig.getAiType()));
            aiConfig.setApiKey(configAiConfig.getApiKey());
            aiConfig.setModel(configAiConfig.getModel());
            return aiConfig;
        }
        return null;
    }

    public boolean studyVideo(YinghuaUserCache userCache, YinghuaNode node) {
        if (node.getTabVideo() == null || !node.getTabVideo()) {
            return false;
        }

        if (node.getProgress() != null && node.getProgress() >= 100) {
            return true;
        }

        if (node.getNodeLock() != null && node.getNodeLock() == 1) {
            log.warn("[{}] 视频 [{}] 已锁定，跳过", userCache.getAccount(), node.getName());
            return false;
        }

        Integer videoDurationObj = node.getVideoDuration();
        Integer viewedDurationObj = node.getViewedDuration();

        if (videoDurationObj == null || videoDurationObj <= 0) {
            log.warn("[{}] 视频 [{}] 时长无效: {}", userCache.getAccount(), node.getName(), videoDurationObj);
            return false;
        }

        int videoDuration = videoDurationObj;
        int currentTime = (viewedDurationObj != null) ? viewedDurationObj : 0;
        String studyId = "0";

        log.info("[{}] 正在学习视频: {}", userCache.getAccount(), node.getName());

        int submitCount = 0;
        int heartbeatCount = 0;

        while (true) {
            if (node.getProgress() != null && node.getProgress() >= 100) {
                log.info("[{}] [{}] 学习完毕", userCache.getAccount(), node.getName());
                break;
            }

            currentTime += SUBMIT_INTERVAL_SECONDS;
            if (currentTime > videoDuration) {
                currentTime = videoDuration;
            }

            try {
                String response = yinghuaStudyApi.submitStudyTime(userCache, node.getId(), studyId, currentTime);
                submitCount++;
                heartbeatCount++;

                if (heartbeatCount >= 30) {
                    heartbeatCount = 0;
                    try {
                        String heartbeatResponse = yinghuaLoginApi.keepAlive(userCache);
                        log.info("[{}] 登录心跳保活状态: {}", userCache.getAccount(), heartbeatResponse);
                    } catch (Exception e) {
                        log.warn("[{}] 心跳保活失败: {}", userCache.getAccount(), e.getMessage());
                    }
                }

                if (response != null && response.contains("\"status\":true")) {
                    double progress = (double) currentTime / videoDuration * 100.0;
                    log.info("[{}] [{}] >>> 提交状态：提交学时成功! 观看时间：{}/{} 观看进度：{}%",
                            userCache.getAccount(), node.getName(), currentTime, videoDuration, String.format("%.2f", progress));

                    JSONObject jsonResponse = JsonUtil.parseObject(response);
                    JSONObject result = jsonResponse.getJSONObject("result");
                    if (result != null && result.containsKey("data")) {
                        JSONObject data = result.getJSONObject("data");
                        if (data != null && data.containsKey("studyId")) {
                            Object studyIdObj = data.get("studyId");
                            if (studyIdObj != null) {
                                studyId = String.valueOf(studyIdObj);
                            }
                        }
                    }

                    if (currentTime >= videoDuration) {
                        break;
                    }
                } else {
                    String msg = "";
                    if (response != null) {
                        JSONObject jsonResponse = JsonUtil.parseObject(response);
                        msg = jsonResponse.getString("msg");
                    }
                    
                    if (msg != null && (msg.contains("未到") || msg.contains("未开始"))) {
                        throw new CourseNotStartedException(msg);
                    }
                    
                    log.warn("[{}] [{}] >>> 提交状态：{}", userCache.getAccount(), node.getName(), response);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
            } catch (CourseNotStartedException e) {
                throw e;
            } catch (Exception e) {
                log.error("[{}] [{}] 提交学时异常: {}", userCache.getAccount(), node.getName(), e.getMessage());
            }

            if (currentTime < videoDuration) {
                try {
                    Thread.sleep(SUBMIT_INTERVAL_SECONDS * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("[{}] 视频学习被中断", userCache.getAccount());
                    break;
                }
            }
        }

        return true;
    }

    private boolean handleExam(YinghuaUserCache userCache, String courseId, YinghuaNode node, AiConfig aiConfig) {
        log.info("[{}] 开始处理考试: {}", userCache.getAccount(), node.getName());
        try {
            yinghuaAiService.autoAnswerExam(userCache, courseId, node.getId(), node.getId(), aiConfig, true);
            log.info("[{}] 考试 [{}] 完成", userCache.getAccount(), node.getName());
            return true;
        } catch (Exception e) {
            log.error("[{}] 考试 [{}] 处理失败: {}", userCache.getAccount(), node.getName(), e.getMessage());
            return false;
        }
    }

    private boolean handleWork(YinghuaUserCache userCache, String courseId, YinghuaNode node, AiConfig aiConfig) {
        log.info("[{}] 开始处理作业: {}", userCache.getAccount(), node.getName());
        try {
            yinghuaAiService.autoAnswerWork(userCache, node.getId(), node.getId(), aiConfig);
            log.info("[{}] 作业 [{}] 完成", userCache.getAccount(), node.getName());
            return true;
        } catch (Exception e) {
            log.error("[{}] 作业 [{}] 处理失败: {}", userCache.getAccount(), node.getName(), e.getMessage());
            return false;
        }
    }

    private List<YinghuaNode> getFullVideoList(YinghuaUserCache userCache, String courseId) {
        List<YinghuaNode> nodeList = new ArrayList<>();
        Map<String, Integer> nodeIndexMap = new HashMap<>();

        List<YinghuaNode> baseList = yinghuaCourseApi.getVideoList(userCache, courseId);
        if (baseList != null) {
            for (int i = 0; i < baseList.size(); i++) {
                YinghuaNode node = baseList.get(i);
                nodeList.add(node);
                if (node.getId() != null) {
                    nodeIndexMap.put(node.getId(), i);
                }
            }
        }

        enrichWithWatchRecord(userCache, courseId, nodeList, nodeIndexMap);
        enrichWithWatchRecordPC(userCache, courseId, nodeList, nodeIndexMap);

        return nodeList;
    }

    private void enrichWithWatchRecord(YinghuaUserCache userCache, String courseId,
                                       List<YinghuaNode> nodeList, Map<String, Integer> nodeIndexMap) {
        int page = 1;
        int totalPages = 999;

        while (page <= totalPages) {
            try {
                String response = yinghuaStudyApi.getVideoWatchRecord(userCache, courseId, page);
                JSONObject jsonResponse = JsonUtil.parseObject(response);

                if (!"获取数据成功".equals(jsonResponse.getString("msg"))) {
                    break;
                }

                JSONObject result = jsonResponse.getJSONObject("result");
                if (result == null) break;

                JSONObject pageInfo = result.getJSONObject("pageInfo");
                if (pageInfo != null) {
                    totalPages = pageInfo.getIntValue("pageCount");
                }

                JSONArray list = result.getJSONArray("list");
                if (list == null || list.isEmpty()) break;

                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    Integer index = nodeIndexMap.get(String.valueOf(item.getIntValue("id")));

                    if (index != null && index < nodeList.size()) {
                        YinghuaNode node = nodeList.get(index);
                        node.setProgress(item.getFloatValue("progress"));
                        node.setViewedDuration(item.getIntValue("viewedDuration"));
                        node.setState(item.getIntValue("state"));
                    }
                }

                page++;
            } catch (Exception e) {
                log.warn("[{}] 获取视频观看记录失败: {}", userCache.getAccount(), e.getMessage());
                break;
            }
        }
    }

    private void enrichWithWatchRecordPC(YinghuaUserCache userCache, String courseId,
                                         List<YinghuaNode> nodeList, Map<String, Integer> nodeIndexMap) {
        int page = 1;
        int totalPages = 999;

        while (page <= totalPages) {
            try {
                String response = yinghuaStudyApi.getVideoWatchRecordPC(userCache, courseId, page);
                JSONObject jsonResponse = JsonUtil.parseObject(response);

                JSONObject pageInfo = jsonResponse.getJSONObject("pageInfo");
                if (pageInfo != null) {
                    totalPages = pageInfo.getIntValue("pageCount");
                }

                JSONArray list = jsonResponse.getJSONArray("list");
                if (list == null || list.isEmpty()) break;

                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    Integer index = nodeIndexMap.get(item.getString("id"));

                    if (index != null && index < nodeList.size()) {
                        YinghuaNode node = nodeList.get(index);
                        node.setErrorCode(item.getIntValue("error"));
                        if (item.getString("errorMessage") != null) {
                            node.setErrorMessage(item.getString("errorMessage"));
                        }
                    }
                }

                page++;
            } catch (Exception e) {
                log.warn("[{}] 获取PC端视频观看记录失败: {}", userCache.getAccount(), e.getMessage());
                break;
            }
        }
    }
}
