package com.studyproxy.entity.yinghua;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class YinghuaCourse {

    private String id;

    private String name;

    private Integer mode;

    private Date startDate;

    private Date endDate;

    private Double progress;

    private Integer videoCount;

    private Integer videoLearned;

    private List<YinghuaNode> nodes;
}