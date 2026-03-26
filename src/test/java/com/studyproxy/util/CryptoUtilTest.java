package com.studyproxy.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    @Test
    void testAesEncrypt() {
        String content = "test content";
        String key = "u2oh6Vu^HWe4_AES";
        String encrypted = CryptoUtil.aesEncrypt(content, key);
        assertNotNull(encrypted);
        assertNotEquals(content, encrypted);
    }

    @Test
    void testAesDecrypt() {
        String content = "test content";
        String key = "u2oh6Vu^HWe4_AES";
        String encrypted = CryptoUtil.aesEncrypt(content, key);
        String decrypted = CryptoUtil.aesDecrypt(encrypted, key);
        assertEquals(content, decrypted);
    }

    @Test
    void testMd5() {
        String content = "test content";
        String md5 = CryptoUtil.md5(content);
        assertNotNull(md5);
        assertEquals(32, md5.length());
    }

    @Test
    void testBase64Encode() {
        String content = "test content";
        String encoded = CryptoUtil.base64Encode(content);
        assertNotNull(encoded);
        assertNotEquals(content, encoded);
    }

    @Test
    void testBase64Decode() {
        String content = "test content";
        String encoded = CryptoUtil.base64Encode(content);
        String decoded = CryptoUtil.base64Decode(encoded);
        assertEquals(content, decoded);
    }
}