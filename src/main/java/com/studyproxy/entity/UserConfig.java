package com.studyproxy.entity;

import lombok.Data;

import java.util.List;

@Data
public class UserConfig {

    private String accountType;

    private String url;

    private String account;

    private String password;

    private Integer isProxy = 0;

    private List<String> informEmails;

    private CoursesCustom coursesCustom;

    @Data
    public static class CoursesCustom {
        private String studyTime;
        private Integer shuffleSw = 0;
        private Integer videoModel = 1;
        private Integer autoExam = 0;
        private Integer examAutoSubmit = 1;
        private List<String> includeCourses;
        private List<String> excludeCourses;
    }
}