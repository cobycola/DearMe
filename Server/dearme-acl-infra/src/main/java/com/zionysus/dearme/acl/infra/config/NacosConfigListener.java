package com.zionysus.dearme.acl.infra.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zionysus.dearme.acl.infra.ai.worker.DeepSeekConfig;
import com.zionysus.dearme.acl.infra.ai.worker.OllamaConfig;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Nacos 配置覆盖层 Listener。
 *
 * 职责：连上 Nacos 后订阅 dataId，收到推下来的 properties text 解析后按字段 set 给
 * {@link OllamaConfig} / {@link DeepSeekConfig}。
 *   - 本地无 Nacos（NACOS_SERVER_ADDR 为空）：不启 listener，仅 log，Spring 走 application defaults 起得来
 *   - 任何失败（连接/解析/异常）：只 log warn，不抛（守「业务点不抛异常」反馈记忆）
 *
 * 不存密钥：deepseek.api-key 走 env var，不通过 Nacos 推。
 *
 * 范围外（见 Plan §9）：
 *   - OllamaChatModel bean 启动后 immutable，推 model 名不自动换 bean。本 Listener 仍 set 进 OllamaConfig，
 *     但 Worker 是否真用它取决于实现（当前 OllamaWorker 不读）
 *   - 真动态切 model 需 @RefreshScope 重建 bean，本 Plan 未做
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NacosConfigListener extends ConfigListener {

    private final OllamaConfig ollamaConfig;
    private final DeepSeekConfig deepseekConfig;

    @Value("${nacos.config.server-addr:${NACOS_SERVER_ADDR:}}")
    private String serverAddr;

    @Value("${nacos.config.data-id:dearme-ai-model.properties}")
    private String dataId;

    @Value("${nacos.config.group:DEFAULT_GROUP}")
    private String group;

    private ConfigService configService;

    /**
     * Spring 容器就绪后启动订阅。返回 void 不抛异常，本地无 Nacos 时仅 log。
     */
    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (serverAddr == null || serverAddr.isBlank()) {
            log.info("[NacosConfigListener] NACOS_SERVER_ADDR 未配置，不启 listener，走 application defaults");
            return;
        }
        try {
            Properties props = new Properties();
            props.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
            configService = createConfigService(props);

            // 先拉一次当前配置（避免 listener 漏掉已存在的配置项 startup snapshot）
            String initial = configService.getConfigAndSignListener(dataId, group, 5000, new Listener() {
                @Override
                public Executor getExecutor() { return null; }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    applyConfig(configInfo);
                }
            });
            applyConfig(initial);

            log.info("[NacosConfigListener] 订阅成功 dataId={} group={} server={}", dataId, group, serverAddr);
        } catch (NacosException e) {
            log.warn("[NacosConfigListener] 订阅失败 server={} reason={}，启动期走 application defaults", serverAddr, e.getMessage());
        }
    }

    @Override
    public void applyConfig(String configInfo) {
        if (configInfo == null || configInfo.isBlank()) {
            log.info("[NacosConfigListener] 推下来空配置，不覆盖当前配置");
            return;
        }
        try {
            Properties properties = parseProperties(configInfo);
            applyOllama(properties);
            applyDeepseek(properties);
            log.info("[NacosConfigListener] 配置已更新 ollama.enabled={} deepseek.enabled={}",
                    ollamaConfig.isEnabled(), deepseekConfig.isEnabled());
        } catch (Exception e) {
            log.warn("[NacosConfigListener] 解析配置失败，保留当前配置不变 reason={}", e.getMessage());
        }
    }

    /** 只覆盖推下来的键，缺失键保持当前值，避免部分推送重置字段。 */
    private void applyOllama(Properties properties) {
        OllamaConfig parsed = OllamaConfig.build(properties);
        if (properties.containsKey("dearme.ai.backends.ollama.enabled")) {
            ollamaConfig.setEnabled(parsed.isEnabled());
        }
        if (properties.containsKey("spring.ai.ollama.chat.options.model")) {
            ollamaConfig.setModel(parsed.getModel());
        }
        if (properties.containsKey("spring.ai.ollama.base-url")) {
            ollamaConfig.setBaseUrl(parsed.getBaseUrl());
        }
        if (properties.containsKey("spring.ai.ollama.chat.options.temperature")) {
            ollamaConfig.setTemperature(parsed.getTemperature());
        }
    }

    /** 只覆盖推下来的键，缺失键保持当前值，避免部分推送重置字段。 */
    private void applyDeepseek(Properties properties) {
        DeepSeekConfig parsed = DeepSeekConfig.build(properties);
        if (properties.containsKey("dearme.ai.backends.deepseek.enabled")) {
            deepseekConfig.setEnabled(parsed.isEnabled());
        }
        if (properties.containsKey("spring.ai.deepseek.chat.options.model")) {
            deepseekConfig.setModel(parsed.getModel());
        }
        if (properties.containsKey("spring.ai.deepseek.chat.options.temperature")) {
            deepseekConfig.setTemperature(parsed.getTemperature());
        }
    }

    @Override
    @PreDestroy
    public void shutdown() {
        if (configService != null) {
            try {
                configService.shutDown();
            } catch (NacosException e) {
                log.warn("[NacosConfigListener] 关闭 ConfigService 失败 reason={}", e.getMessage());
            }
        }
    }

    /** 抽出便于测试覆盖：返回 mock ConfigService 替真 Nacos 连接。 */
    protected ConfigService createConfigService(Properties props) throws NacosException {
        return NacosFactory.createConfigService(props);
    }
}
