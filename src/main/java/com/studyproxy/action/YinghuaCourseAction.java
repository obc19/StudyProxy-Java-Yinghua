package com.studyproxy.action;

import com.alibaba.fastjson2.JSONObject;
import com.studyproxy.api.yinghua.YinghuaCourseApi;
import com.studyproxy.entity.yinghua.YinghuaCourse;
import com.studyproxy.entity.yinghua.YinghuaNode;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class YinghuaCourseAction {

    @Resource
    private YinghuaCourseApi yinghuaCourseApi;

    public List<YinghuaCourse> pullCourseList(YinghuaUserCache userCache) {
        List<YinghuaCourse> courseList = yinghuaCourseApi.getCourseList(userCache);
        log.info("[英华学堂] {} 获取到 {} 门课程", userCache.getAccount(), courseList.size());
        return courseList;
    }

    public YinghuaCourse pullCourseDetail(YinghuaUserCache userCache, String courseId) {
        YinghuaCourse yinghuaCourse = yinghuaCourseApi.getCourseDetail(userCache, courseId);
        log.info("[英华学堂] {} 获取课程详情: {}", userCache.getAccount(), courseId);
        return yinghuaCourse;
    }

    public List<YinghuaNode> pullVideoList(YinghuaUserCache userCache, String courseId) {
        List<YinghuaNode> nodeList = yinghuaCourseApi.getVideoList(userCache, courseId);
        log.info("[英华学堂] {} 获取到 {} 个视频节点", userCache.getAccount(), nodeList.size());
        return nodeList;
    }
}
