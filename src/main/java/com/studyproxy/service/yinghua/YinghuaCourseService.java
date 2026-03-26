package com.studyproxy.service.yinghua;

import com.studyproxy.api.yinghua.YinghuaCourseApi;
import com.studyproxy.entity.yinghua.YinghuaCourse;
import com.studyproxy.entity.yinghua.YinghuaNode;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class YinghuaCourseService {

    @Resource
    private YinghuaCourseApi yinghuaCourseApi;

    public List<YinghuaCourse> getCourseList(YinghuaUserCache userCache) {
        log.debug("[{}] 开始获取课程列表", userCache.getAccount());

        List<YinghuaCourse> courseList = yinghuaCourseApi.getCourseList(userCache);

        log.info("[{}] 获取到 {} 门课程", userCache.getAccount(), courseList.size());

        return courseList;
    }

    public YinghuaCourse getCourseDetail(YinghuaUserCache userCache, String courseId) {
        log.debug("[{}] 开始获取课程详情: courseId={}", userCache.getAccount(), courseId);

        YinghuaCourse course = yinghuaCourseApi.getCourseDetail(userCache, courseId);

        log.info("[{}] 获取课程详情成功: {}", userCache.getAccount(), course.getName());

        return course;
    }

    public List<YinghuaNode> getVideoList(YinghuaUserCache userCache, String courseId) {
        log.debug("[{}] 开始获取视频列表: courseId={}", userCache.getAccount(), courseId);

        List<YinghuaNode> nodeList = yinghuaCourseApi.getVideoList(userCache, courseId);

        log.info("[{}] 获取到 {} 个视频节点", userCache.getAccount(), nodeList.size());

        return nodeList;
    }
}
