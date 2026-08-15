package com.zionysus.dearme.south.adapter.nacos;

import com.zionysus.dearme.south.adapter.ai.worker.AiModelConfig;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Nacos 配置覆盖层 Listener。
 *
 * 职责：连上 Nacos 后订阅 dataId，收到推下来的 properties text 解析后整体 set 给 {@link AiModelConfig}。
 *   - 本地无 Nacos（NACOS_SERVER_ADDR 为空）：不启 listener，仅 log，Spring 走 application defaults 起得来
 *   - 任何失败（连接/解析/异常）：只 log warn，不抛（守「业务点不抛异常」反馈记忆）
 *
 * 不存密钥：deepseek.api-key 走 env var，不通过 Nacos 推。
 *
 * 范围外（见 Plan §9）：
 *   - OllamaChatModel bean 启动后 immutable，推 model 名不自动换 bean。本 Listener 仍 set 进 AiModelConfig，
 *     但 Worker 是否真用它取决于实现（当前 OllamaWorker 不读）
 *   - 真动态切 model 需 @RefreshScope 重建 bean，本 Plan 未做
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NacosConfigListener {

    private final AiModelConfig aiModelConfig;

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
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
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

    private void applyConfig(String configInfo) {
        if (configInfo == null || configInfo.isBlank()) {
            log.info("[NacosConfigListener] 推下来空配置，不覆盖当前 AiModelConfig");
            return;
        }
        try {
            Properties p = new Properties();
            p.load(new StringReader(configInfo));

            if (p.containsKey("dearme.ai.backends.ollama.enabled")) {
                aiModelConfig.setOllamaEnabled(Boolean.parseBoolean(p.getProperty("dearme.ai.backends.ollama.enabled")));
            }
            if (p.containsKey("dearme.ai.backends.deepseek.enabled")) {
                aiModelConfig.setDeepseekEnabled(Boolean.parseBoolean(p.getProperty("dearme.ai.backends.deepseek.enabled")));
            }
            if (p.containsKey("spring.ai.ollama.chat.options.model")) {
                aiModelConfig.setOllamaModel(p.getProperty("spring.ai.ollama.chat.options.model"));
            }
            if (p.containsKey("spring.ai.ollama.base-url")) {
                aiModelConfig.setOllamaBaseUrl(p.getProperty("spring.ai.ollama.base-url"));
            }
            if (p.containsKey("spring.ai.ollama.chat.options.temperature")) {
                aiModelConfig.setOllamaTemperature(Double.parseDouble(p.getProperty("spring.ai.ollama.chat.options.temperature")));
            }
            if (p.containsKey("spring.ai.deepseek.chat.options.model")) {
                aiModelConfig.setDeepseekModel(p.getProperty("spring.ai.deepseek.chat.options.model"));
            }
            if (p.containsKey("spring.ai.deepseek.chat.options.temperature")) {
                aiModelConfig.setDeepseekTemperature(Double.parseDouble(p.getProperty("spring.ai.deepseek.chat.options.temperature")));
            }
            log.info("[NacosConfigListener] 配置已更新 ollama.enabled={} deepseek.enabled={}",
                    aiModelConfig.isOllamaEnabled(), aiModelConfig.isDeepseekEnabled());
        } catch (Exception e) {
            log.warn("[NacosConfigListener] 解析配置失败，保留当前 AiModelConfig 不变 reason={}", e.getMessage());
        }
    }

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