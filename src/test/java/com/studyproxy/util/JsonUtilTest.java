package com.studyproxy.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void testToJsonString() {
        TestObject obj = new TestObject("test", 123);
        String json = JsonUtil.toJsonString(obj);
        assertNotNull(json);
        assertTrue(json.contains("test"));
    }

    @Test
    void testParseObject() {
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject obj = JsonUtil.parseObject(json, TestObject.class);
        assertNotNull(obj);
        assertEquals("test", obj.getName());
        assertEquals(123, obj.getValue());
    }

    @Test
    void testIsValidJson() {
        String validJson = "{\"name\":\"test\",\"value\":123}";
        String invalidJson = "not a json";
        assertTrue(JsonUtil.isValidJson(validJson));
        assertFalse(JsonUtil.isValidJson(invalidJson));
    }

    static class TestObject {
        private String name;
        private Integer value;

        public TestObject() {}

        public TestObject(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}