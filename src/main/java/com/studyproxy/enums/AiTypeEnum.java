package com.studyproxy.enums;

public enum AiTypeEnum {
    TONGYI("TONGYI", "通义千问"),
    CHATGLM("CHATGLM", "智谱ChatGLM"),
    XINGHUO("XINGHUO", "讯飞星火"),
    DOUBAO("DOUBAO", "字节豆包"),
    OPENAI("OPENAI", "OpenAI"),
    DEEPSEEK("DEEPSEEK", "DeepSeek"),
    SILICON("SILICON", "SiliconFlow"),
    META_AI("META_AI", "MetaAI"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String name;

    AiTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AiTypeEnum fromCode(String code) {
        for (AiTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
