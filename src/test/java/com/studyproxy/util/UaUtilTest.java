package com.studyproxy.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UaUtilTest {

    @Test
    void testGetMobileUA() {
        String ua = UaUtil.getMobileUA("MI10", "6.7.2", "10831_263", "1234567890abcdef");
        assertNotNull(ua);
        assertTrue(ua.contains("Mozilla"));
        assertTrue(ua.contains("ChaoXing"));
    }

    @Test
    void testGetWebUA() {
        String ua = UaUtil.getWebUA();
        assertNotNull(ua);
        assertTrue(ua.contains("Mozilla"));
        assertTrue(ua.contains("Chrome"));
    }

    @Test
    void testGetDefaultUA() {
        String ua = UaUtil.getDefaultUA();
        assertNotNull(ua);
        assertTrue(ua.contains("Mozilla"));
    }

    @Test
    void testGenerateImei() {
        String imei = UaUtil.generateImei();
        assertNotNull(imei);
        assertEquals(16, imei.length());
    }

    @Test
    void testGenerate17DigitNumber() {
        String number = UaUtil.generate17DigitNumber();
        assertNotNull(number);
        assertEquals(17, number.length());
        assertTrue(number.matches("\\d{17}"));
    }
}