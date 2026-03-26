package com.studyproxy.service.yinghua;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaStudyApi;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class YinghuaStudyService {

    @Resource
    private YinghuaStudyApi yinghuaStudyApi;

    public String submitStudyTime(YinghuaUserCache userCache, String nodeId, String studyId, Integer studyTime) {
        log.debug("[{}] 提交学时: nodeId={}, studyTime={}", userCache.getAccount(), nodeId, studyTime);

        String response = yinghuaStudyApi.submitStudyTime(userCache, nodeId, studyId, studyTime);

        JSONObject jsonResponse = JsonUtil.parseObject(response);
        if (jsonResponse.getBoolean("status") != null && jsonResponse.getBoolean("status")) {
            log.info("[{}] 学时提交成功", userCache.getAccount());
            return response;
        }

        throw new RuntimeException("学时提交失败");
    }

    public String getVideoStudyTime(YinghuaUserCache userCache, String nodeId) {
        return yinghuaStudyApi.getVideoStudyTime(userCache, nodeId);
    }

    public String getVideoWatchRecord(YinghuaUserCache userCache, String courseId, int page) {
        log.debug("[{}] 获取视频观看记录: courseId={}, page={}", userCache.getAccount(), courseId, page);
        return yinghuaStudyApi.getVideoWatchRecord(userCache, courseId, page);
    }

    public String getVideoWatchRecordPC(YinghuaUserCache userCache, String courseId, int page) {
        log.debug("[{}] 获取视频观看记录(PC): courseId={}, page={}", userCache.getAccount(), courseId, page);
        return yinghuaStudyApi.getVideoWatchRecordPC(userCache, courseId, page);
    }
}
