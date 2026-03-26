package com.studyproxy.entity.yinghua;

import lombok.Data;

import java.util.Date;

@Data
public class YinghuaWork {

    private String id;

    private String workId;

    private String nodeId;

    private String courseId;

    private String title;

    private Date startTime;

    private Date endTime;

    private Float score;

    private Integer allow;

    private Integer frequency;
}