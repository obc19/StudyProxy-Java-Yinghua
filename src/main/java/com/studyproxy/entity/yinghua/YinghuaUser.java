package com.studyproxy.entity.yinghua;

import lombok.Data;

@Data
public class YinghuaUser {

    private Long id;

    private String account;

    private String password;

    private String token;

    private String sign;

    private String cookie;

    private Integer status;

    private String createTime;

    private String updateTime;
}