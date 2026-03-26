package com.studyproxy.entity.ai;

import lombok.Data;

@Data
public class AiConfig {
    private AiTypeEnum aiType;
    private String url;
    private String model;
    private String apiKey;

    public enum AiTypeEnum {
        TONGYI("TONGYI", "通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen-plus-latest"),
        CHATGLM("CHATGLM", "智谱ChatGLM", "https://open.bigmodel.cn/api/paas/v4/chat/completions", "glm-4"),
        XINGHUO("XINGHUO", "讯飞星火", "https://spark-api-open.xf-yun.com/v1/chat/completions", "generalv3.5"),
        DOUBAO("DOUBAO", "字节豆包", "https://ark.cn-beijing.volces.com/api/v3/chat/completions", null),
        OPENAI("OPENAI", "OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4"),
        DEEPSEEK("DEEPSEEK", "DeepSeek", "https://api.deepseek.com/chat/completions", "deepseek-chat"),
        SILICON("SILICON", "SiliconFlow", "https://api.siliconflow.cn/v1/chat/completions", null),
        OTHER("OTHER", "其他", null, null);

        private final String code;
        private final String name;
        private final String defaultUrl;
        private final String defaultModel;

        AiTypeEnum(String code, String name, String defaultUrl, String defaultModel) {
            this.code = code;
            this.name = name;
            this.defaultUrl = defaultUrl;
            this.defaultModel = defaultModel;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDefaultUrl() {
            return defaultUrl;
        }

        public String getDefaultModel() {
            return defaultModel;
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
}
