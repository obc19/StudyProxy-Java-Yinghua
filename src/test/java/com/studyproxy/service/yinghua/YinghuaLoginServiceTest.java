package com.studyproxy.service.yinghua;

import com.studyproxy.entity.yinghua.YinghuaUserCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YinghuaLoginServiceTest {

    @Resource
    private YinghuaLoginService yinghuaLoginService;

    private YinghuaUserCache userCache;

    @BeforeEach
    void setUp() {
        userCache = new YinghuaUserCache();
        userCache.setPreUrl("https://yinghua.example.com");
        userCache.setAccount("test_account");
        userCache.setPassword("test_password");
    }

    @Test
    void testLogin() {
        assertThrows(RuntimeException.class, () -> {
            yinghuaLoginService.login(userCache);
        });
    }

    @Test
    void testGetVerificationCodeImage() {
        assertThrows(RuntimeException.class, () -> {
            yinghuaLoginService.getVerificationCodeImage(userCache);
        });
    }
}
