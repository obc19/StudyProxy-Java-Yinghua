package com.studyproxy.entity.yinghua;

import lombok.Data;

import java.util.Date;

@Data
public class YinghuaExam {

    private String id;

    private String examId;

    private String nodeId;

    private String courseId;

    private String title;

    private Date startTime;

    private Date endTime;

    private Float limitedTime;

    private Float score;
}