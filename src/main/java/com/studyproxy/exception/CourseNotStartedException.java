package com.studyproxy.exception;

public class CourseNotStartedException extends RuntimeException {
    public CourseNotStartedException(String message) {
        super(message);
    }
}
