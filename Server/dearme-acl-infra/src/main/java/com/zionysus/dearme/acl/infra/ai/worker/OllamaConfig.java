package com.zionysus.dearme.acl.infra.ai.worker;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Ollama 本地模型后端配置持有（南向 AI 基础设施）。
 *
 * 既是 Spring bean（供 {@link AiRouter} 按 backends 启用状态过滤 Worker、供
 * {@link com.zionysus.dearme.acl.infra.config.NacosConfigListener} 动态覆盖），
 * 也提供显式构造（供 AiRouterTest 装配 mock 场景）。
 */
@Component
@Getter
@Setter
public class OllamaConfig {

    @Value("${dearme.ai.backends.ollama.enabled:false}")
    private boolean enabled;

    @Value("${spring.ai.ollama.chat.options.model:}")
    private String model;

    @Value("${spring.ai.ollama.base-url:}")
    private String baseUrl;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private double temperature;

    public OllamaConfig() {
    }

    public OllamaConfig(boolean enabled, String model, String baseUrl, double temperature) {
        this.enabled = enabled;
        this.model = model;
        this.baseUrl = baseUrl;
        this.temperature = temperature;
    }

    /** 从 Nacos 推下来的 properties 重建一份新配置，调用方自行决定覆盖哪些键。 */
    public static OllamaConfig build(Properties properties) {
        OllamaConfig config = new OllamaConfig();
        if (properties.containsKey("dearme.ai.backends.ollama.enabled")) {
            config.setEnabled(Boolean.parseBoolean(properties.getProperty("dearme.ai.backends.ollama.enabled")));
        }
        if (properties.containsKey("spring.ai.ollama.chat.options.model")) {
            config.setModel(properties.getProperty("spring.ai.ollama.chat.options.model"));
        }
        if (properties.containsKey("spring.ai.ollama.base-url")) {
            config.setBaseUrl(properties.getProperty("spring.ai.ollama.base-url"));
        }
        if (properties.containsKey("spring.ai.ollama.chat.options.temperature")) {
            config.setTemperature(Double.parseDouble(properties.getProperty("spring.ai.ollama.chat.options.temperature")));
        }
        return config;
    }
}
