package com.zionysus.dearme.acl.infra.ai.worker;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * DeepSeek 远端模型后端配置持有（南向 AI 基础设施）。
 *
 * 同 {@link OllamaConfig}：既是 Spring bean，也提供显式构造（供 AiRouterTest 装配）。
 * api-key 不走本类（红线：密钥只走环境变量 DEEPSEEK_API_KEY，不进代码/配置）。
 */
@Component
@Getter
@Setter
public class DeepSeekConfig {

    @Value("${dearme.ai.backends.deepseek.enabled:false}")
    private boolean enabled;

    @Value("${spring.ai.deepseek.chat.options.model:}")
    private String model;

    @Value("${spring.ai.deepseek.chat.options.temperature:0.7}")
    private double temperature;

    public DeepSeekConfig() {
    }

    public DeepSeekConfig(boolean enabled, String model, double temperature) {
        this.enabled = enabled;
        this.model = model;
        this.temperature = temperature;
    }

    /** 从 Nacos 推下来的 properties 重建一份新配置，调用方自行决定覆盖哪些键。 */
    public static DeepSeekConfig build(Properties properties) {
        DeepSeekConfig config = new DeepSeekConfig();
        if (properties.containsKey("dearme.ai.backends.deepseek.enabled")) {
            config.setEnabled(Boolean.parseBoolean(properties.getProperty("dearme.ai.backends.deepseek.enabled")));
        }
        if (properties.containsKey("spring.ai.deepseek.chat.options.model")) {
            config.setModel(properties.getProperty("spring.ai.deepseek.chat.options.model"));
        }
        if (properties.containsKey("spring.ai.deepseek.chat.options.temperature")) {
            config.setTemperature(Double.parseDouble(properties.getProperty("spring.ai.deepseek.chat.options.temperature")));
        }
        return config;
    }
}
