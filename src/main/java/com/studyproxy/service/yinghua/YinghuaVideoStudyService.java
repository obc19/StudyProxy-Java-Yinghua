package com.studyproxy.service.yinghua;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaCourseApi;
import com.studyproxy.api.yinghua.YinghuaLoginApi;
import com.studyproxy.api.yinghua.YinghuaStudyApi;
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

/**
 * 视频学习服务。
 *
 * <p>当前版本只做视频学习：考试与作业的自动答题逻辑已移除。
 */
@Slf4j
@Service
public class YinghuaVideoStudyService {

    /** 每次提交学时的间隔（秒） */
    private static final int SUBMIT_INTERVAL_SECONDS = 5;

    /** 每提交多少次学时做一次登录保活 */
    private static final int HEARTBEAT_INTERVAL_SECONDS = 300;

    /** 连续提交失败的最大次数，超过则放弃当前视频，避免死循环 */
    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    @Resource
    private YinghuaCourseApi yinghuaCourseApi;

    @Resource
    private YinghuaLoginApi yinghuaLoginApi;

    @Resource
    private YinghuaStudyApi yinghuaStudyApi;

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

    /**
     * 学习一门课程中的所有视频节点。
     *
     * <p>关于 nodeLock：英华学堂采用「按顺序解锁」机制，第 N 个视频要在第 N-1 个视频
     * 达到 100% 之后才解锁。章节列表里的 nodeLock 是「拉取那一刻」的快照，遍历过程中
     * 不会自动变化。所以每完成一个视频后，本地快照里的后续节点仍然显示为锁定状态，
     * 如果直接据此跳过，就会导致「一门课只刷一个视频」。
     *
     * <p>修复方式：遇到锁定节点时，若自上次完成视频以来还没刷新过快照，则重新拉取章节
     * 列表，用最新数据判断是否真的还锁定；仍然锁定才跳过。刷新只在「刚完成视频」之后
     * 触发一次，不会对每个节点都发请求。
     */
    public void studyCourse(YinghuaUserCache userCache, YinghuaCourse course) {
        List<YinghuaNode> nodeList = getFullVideoList(userCache, course.getId());
        log.info("[{}] 课程 [{}] 共 {} 个节点", userCache.getAccount(), course.getName(), nodeList.size());

        if (nodeList.isEmpty()) {
            log.warn("[{}] 课程 [{}] 没有找到任何节点", userCache.getAccount(), course.getName());
            return;
        }

        int videoTotal = countVideoNodes(nodeList);
        if (videoTotal == 0) {
            log.info("[{}] 课程 [{}] 没有视频节点，跳过", userCache.getAccount(), course.getName());
            return;
        }

        int videoCompleted = 0;
        int videoLocked = 0;
        int videoFailed = 0;

        // 初始快照刚拉过，是最新的，所以一开始不需要刷新
        boolean needRefresh = false;

        log.info("[{}] 开始遍历 {} 个节点（视频 {} 个）...",
                userCache.getAccount(), nodeList.size(), videoTotal);

        for (int i = 0; i < nodeList.size(); i++) {
            YinghuaNode node = nodeList.get(i);

            if (!isVideoNode(node)) {
                continue;
            }

            if (isFinished(node)) {
                videoCompleted++;
                continue;
            }

            // 遇到锁定节点：若自上次完成视频后还没刷新，就用最新快照重新判断
            if (isLocked(node) && needRefresh) {
                List<YinghuaNode> refreshed = tryRefreshNodeList(userCache, course);
                if (refreshed != null && !refreshed.isEmpty()) {
                    nodeList = refreshed;
                    needRefresh = false;
                    YinghuaNode latest = findByNodeId(nodeList, node.getId());
                    if (latest == null) {
                        log.warn("[{}] 刷新章节列表后未找到节点 [{}]，跳过", userCache.getAccount(), node.getName());
                        continue;
                    }
                    node = latest;
                }
            }

            if (isLocked(node)) {
                videoLocked++;
                log.warn("[{}] 视频 [{}] 已锁定，跳过", userCache.getAccount(), node.getName());
                continue;
            }

            if (isFinished(node)) {
                videoCompleted++;
                continue;
            }

            try {
                if (studyVideo(userCache, node)) {
                    videoCompleted++;
                    // 刚完成一个视频，后续节点可能已解锁，下次遇到锁定时需要刷新快照
                    needRefresh = true;
                } else {
                    videoFailed++;
                }
            } catch (CourseNotStartedException e) {
                log.warn("[{}] 课程 [{}] 未到解锁时间已自动跳过", userCache.getAccount(), course.getName());
                return;
            } catch (Exception e) {
                videoFailed++;
                log.error("[{}] 视频 [{}] 学习失败: {}", userCache.getAccount(), node.getName(), e.getMessage());
            }
        }

        log.info("[{}] 课程 [{}] 完成 - 视频:{}/{} 锁定跳过:{} 失败:{}",
                userCache.getAccount(), course.getName(),
                videoCompleted, videoTotal, videoLocked, videoFailed);
    }

    /**
     * 学习单个视频。
     *
     * @return 是否真正达到了 100%（锁定 / 失败 / 中断都返回 false）
     */
    public boolean studyVideo(YinghuaUserCache userCache, YinghuaNode node) {
        if (!isVideoNode(node)) {
            return false;
        }

        if (isFinished(node)) {
            return true;
        }

        if (isLocked(node)) {
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
        if (currentTime < 0) {
            currentTime = 0;
        }
        String studyId = "0";

        log.info("[{}] 正在学习视频: {}", userCache.getAccount(), node.getName());

        int submitCount = 0;
        int heartbeatCount = 0;
        int consecutiveFailures = 0;
        boolean completed = false;

        while (true) {
            if (isFinished(node)) {
                log.info("[{}] [{}] 学习完毕", userCache.getAccount(), node.getName());
                completed = true;
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
                    consecutiveFailures = 0;
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
                        completed = true;
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

                    consecutiveFailures++;
                    log.warn("[{}] [{}] >>> 提交状态：{}", userCache.getAccount(), node.getName(), response);

                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        log.error("[{}] [{}] 连续 {} 次提交失败，放弃该视频本次学习",
                                userCache.getAccount(), node.getName(), consecutiveFailures);
                        break;
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
            } catch (CourseNotStartedException e) {
                throw e;
            } catch (Exception e) {
                consecutiveFailures++;
                log.error("[{}] [{}] 提交学时异常: {}", userCache.getAccount(), node.getName(), e.getMessage());
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    log.error("[{}] [{}] 连续 {} 次提交异常，放弃该视频本次学习",
                            userCache.getAccount(), node.getName(), consecutiveFailures);
                    break;
                }
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

        return completed;
    }

    /** 是否为视频节点 */
    private boolean isVideoNode(YinghuaNode node) {
        return node != null && Boolean.TRUE.equals(node.getTabVideo());
    }

    /** 是否已学完 */
    private boolean isFinished(YinghuaNode node) {
        return node != null && node.getProgress() != null && node.getProgress() >= 100;
    }

    /** 是否处于锁定状态 */
    private boolean isLocked(YinghuaNode node) {
        return node != null && node.getNodeLock() != null && node.getNodeLock() == 1;
    }

    private int countVideoNodes(List<YinghuaNode> nodeList) {
        int count = 0;
        for (YinghuaNode node : nodeList) {
            if (isVideoNode(node)) {
                count++;
            }
        }
        return count;
    }

    private YinghuaNode findByNodeId(List<YinghuaNode> nodeList, String nodeId) {
        if (nodeId == null) {
            return null;
        }
        for (YinghuaNode node : nodeList) {
            if (nodeId.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    /** 重新拉取章节列表；失败时返回 null，调用方沿用旧快照 */
    private List<YinghuaNode> tryRefreshNodeList(YinghuaUserCache userCache, YinghuaCourse course) {
        try {
            List<YinghuaNode> refreshed = getFullVideoList(userCache, course.getId());
            if (refreshed != null && !refreshed.isEmpty()) {
                log.info("[{}] 已刷新课程 [{}] 的章节列表，重新判断解锁状态",
                        userCache.getAccount(), course.getName());
            }
            return refreshed;
        } catch (Exception e) {
            log.warn("[{}] 刷新课程 [{}] 章节列表失败: {}",
                    userCache.getAccount(), course.getName(), e.getMessage());
            return null;
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
