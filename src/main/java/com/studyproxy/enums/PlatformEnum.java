package com.studyproxy.enums;

import lombok.Getter;

@Getter
public enum PlatformEnum {

    YINGHUA("英华学堂", "YINGHUA");

    private final String name;

    private final String code;

    PlatformEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static PlatformEnum fromCode(String code) {
        for (PlatformEnum platform : values()) {
            if (platform.code.equals(code)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("不支持的平台: " + code);
    }
}
