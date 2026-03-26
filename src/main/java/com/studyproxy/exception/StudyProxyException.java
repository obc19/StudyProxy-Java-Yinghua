package com.studyproxy.exception;

import lombok.Getter;

@Getter
public class StudyProxyException extends RuntimeException {

    private final int code;

    private final String message;

    public StudyProxyException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public StudyProxyException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public StudyProxyException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    public StudyProxyException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}