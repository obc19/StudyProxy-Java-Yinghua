package com.studyproxy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudyProxyApplicationTest {

    @Test
    void contextLoads() {
        assertNotNull(StudyProxyApplication.class);
    }
}