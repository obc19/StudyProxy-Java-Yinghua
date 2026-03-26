package com.studyproxy.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

    public static String toJsonString(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", json, e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return JSON.parseObject(json, typeReference);
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", json, e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    public static JSONObject parseObject(String json) {
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.error("JSON解析失败: {}", json, e);
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    public static boolean isValidJson(String json) {
        try {
            JSON.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}