package com.studyproxy.action;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaStudyApi;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class YinghuaStudyAction {

    @Resource
    private YinghuaStudyApi yinghuaStudyApi;

    public String submitStudyTime(YinghuaUserCache userCache, String nodeId, String studyId, Integer studyTime) {
        int retryCount = 0;

        while (retryCount < 3) {
            try {
                String response = yinghuaStudyApi.submitStudyTime(userCache, nodeId, studyId, studyTime);

                JSONObject jsonResponse = JSONObject.parseObject(response);
                if (jsonResponse.getBoolean("status") != null && jsonResponse.getBoolean("status")) {
                    log.info("[英华学堂] {} 进度提交成功 - 已学习 {} 秒", userCache.getAccount(), studyTime);
                    return response;
                }

                String msg = jsonResponse.getString("msg");
                if (msg != null) {
                    throw new RuntimeException(msg);
                }

                throw new RuntimeException("学时提交失败");
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= 3) {
                    log.info("[英华学堂] {} 进度提交失败: {}", userCache.getAccount(), e.getMessage());
                    throw new RuntimeException("学时提交失败: " + e.getMessage());
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException("学时提交失败");
    }

    public String getVideoStudyTime(YinghuaUserCache userCache, String nodeId) {
        String response = yinghuaStudyApi.getVideoStudyTime(userCache, nodeId);
        log.info("[英华学堂] {} 获取视频学习进度", userCache.getAccount());
        return response;
    }
}
