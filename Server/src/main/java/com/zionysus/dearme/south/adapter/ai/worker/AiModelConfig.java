package com.zionysus.dearme.south.adapter.ai.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI 模型参数运行时态（单例，被 AiRouter 入口过滤时读、被 NacosConfigListener 推时整体替换字段）。
 *
 * 设计要点：
 *   - 字段 volatile：每题并发读，Nacos 推时按字段整体写一次。放弃跨字段强一致性
 *     （单字段 volatile 即可，跨字段交叉读容忍 torn state，业务降级路径自然兜住）
 *   - 默认值来自 application*.properties（启动装配阶段注入），Nacos 推下来覆盖
 *   - 不存密钥、不存 baseUrl：deepseek.api-key 走 env var，base-url 启动级
 *
 * 范围外说明（见 Plan §9）：
 *   - spring.ai.ollama.chat.options.model 推下来后 OllamaChatModel bean 已 immutable，真换 model 名要重建 bean；
 *     本字段 listener 仍 set 进来，但 Worker 用它做什么取决于实现（当前 OllamaWorker 不读它）
 *   - 真动态生效的是 backends.*.enabled → AiRouter 入口过滤
 */
@Slf4j
@Component
public class AiModelConfig {

    private volatile boolean ollamaEnabled;
    private volatile boolean deepseekEnabled;
    private volatile String ollamaModel;
    private volatile String ollamaBaseUrl;
    private volatile double ollamaTemperature;
    private volatile String deepseekModel;
    private volatile double deepseekTemperature;

    public AiModelConfig(
            @Value("${dearme.ai.backends.ollama.enabled:false}") boolean ollamaEnabled,
            @Value("${dearme.ai.backends.deepseek.enabled:false}") boolean deepseekEnabled,
            @Value("${spring.ai.ollama.chat.options.model:}") String ollamaModel,
            @Value("${spring.ai.ollama.base-url:}") String ollamaBaseUrl,
            @Value("${spring.ai.ollama.chat.options.temperature:0.7}") double ollamaTemperature,
            @Value("${spring.ai.deepseek.chat.options.model:}") String deepseekModel,
            @Value("${spring.ai.deepseek.chat.options.temperature:0.7}") double deepseekTemperature
    ) {
        this.ollamaEnabled = ollamaEnabled;
        this.deepseekEnabled = deepseekEnabled;
        this.ollamaModel = ollamaModel;
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.ollamaTemperature = ollamaTemperature;
        this.deepseekModel = deepseekModel;
        this.deepseekTemperature = deepseekTemperature;
        log.info("[AiModelConfig] defaults ollama.enabled={} deepseek.enabled={}", ollamaEnabled, deepseekEnabled);
    }

    public boolean isBackendEnabled(String backendKey) {
        if ("ollama".equals(backendKey)) return ollamaEnabled;
        if ("deepseek".equals(backendKey)) return deepseekEnabled;
        return true;
    }

    public boolean isOllamaEnabled() { return ollamaEnabled; }
    public boolean isDeepseekEnabled() { return deepseekEnabled; }
    public String getOllamaModel() { return ollamaModel; }
    public String getOllamaBaseUrl() { return ollamaBaseUrl; }
    public double getOllamaTemperature() { return ollamaTemperature; }
    public String getDeepseekModel() { return deepseekModel; }
    public double getDeepseekTemperature() { return deepseekTemperature; }

    public void setOllamaEnabled(boolean v) { this.ollamaEnabled = v; }
    public void setDeepseekEnabled(boolean v) { this.deepseekEnabled = v; }
    public void setOllamaModel(String v) { this.ollamaModel = v; }
    public void setOllamaBaseUrl(String v) { this.ollamaBaseUrl = v; }
    public void setOllamaTemperature(double v) { this.ollamaTemperature = v; }
    public void setDeepseekModel(String v) { this.deepseekModel = v; }
    public void setDeepseekTemperature(double v) { this.deepseekTemperature = v; }
}