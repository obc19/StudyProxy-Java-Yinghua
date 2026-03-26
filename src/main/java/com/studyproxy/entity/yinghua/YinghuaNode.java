package com.studyproxy.entity.yinghua;

import lombok.Data;

import java.util.Date;

@Data
public class YinghuaNode {

    private String id;

    private String courseId;

    private String chapterId;

    private String name;

    private Integer videoDuration;

    private Integer nodeLock;

    private Date unlockTime;

    private Float progress;

    private Integer viewedDuration;

    private Integer state;

    private Integer errorCode;

    private String errorMessage;

    private Boolean tabVideo;

    private Boolean tabFile;

    private Boolean tabVote;

    private Boolean tabWork;

    private Boolean tabExam;
}