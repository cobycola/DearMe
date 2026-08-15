package com.zionysus.dearme.south.adapter.nacos;

import com.zionysus.dearme.south.adapter.ai.worker.AiModelConfig;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NacosConfigListenerTest {

    @Test
    void doesNothingWhenServerAddrBlank() {
        AiModelConfig cfg = new AiModelConfig(true, false, "m", "u", 0.7, "d", 0.7);
        NacosConfigListener listener = new NacosConfigListener(cfg);
        ReflectionTestUtils.setField(listener, "serverAddr", "");
        ReflectionTestUtils.setField(listener, "dataId", "x");
        ReflectionTestUtils.setField(listener, "group", "g");

        listener.start(); // 不应连 Nacos

        assertThat(cfg.isOllamaEnabled()).isTrue(); // defaults 保留
    }

    @Test
    void appliesValidConfigFromNacos() throws Exception {
        AiModelConfig cfg = new AiModelConfig(true, false, "qwen2.5:7b", "http://localhost:11434", 0.7, "d", 0.7);

        AtomicReference<Listener> captured = new AtomicReference<>();
        ConfigService mockService = mock(ConfigService.class);
        String initial = """
                dearme.ai.backends.ollama.enabled=false
                dearme.ai.backends.deepseek.enabled=true
                spring.ai.ollama.chat.options.model=qwen2.5:14b
                spring.ai.ollama.chat.options.temperature=0.3
                """;
        when(mockService.getConfigAndSignListener(anyString(), anyString(), anyLong(), any())).thenAnswer(inv -> {
            captured.set(inv.getArgument(3));
            return initial;
        });

        NacosConfigListener listener = new NacosConfigListener(cfg) {
            @Override protected ConfigService createConfigService(Properties props) { return mockService; }
        };
        ReflectionTestUtils.setField(listener, "serverAddr", "127.0.0.1:8848");
        ReflectionTestUtils.setField(listener, "dataId", "dearme-ai-model.properties");
        ReflectionTestUtils.setField(listener, "group", "DEFAULT_GROUP");

        listener.start();

        assertThat(cfg.isOllamaEnabled()).isFalse();
        assertThat(cfg.isDeepseekEnabled()).isTrue();
        assertThat(cfg.getOllamaModel()).isEqualTo("qwen2.5:14b");
        assertThat(cfg.getOllamaTemperature()).isEqualTo(0.3);

        // 模拟 Nacos 后续推一次新配置：把 ollama 重新打开
        captured.get().receiveConfigInfo("dearme.ai.backends.ollama.enabled=true");
        assertThat(cfg.isOllamaEnabled()).isTrue();
    }

    @Test
    void swallowsBadConfigWithoutBreakingExistingState() throws Exception {
        AiModelConfig cfg = new AiModelConfig(true, false, "qwen2.5:7b", "u", 0.7, "d", 0.7);

        ConfigService mockService = mock(ConfigService.class);
        when(mockService.getConfigAndSignListener(anyString(), anyString(), anyLong(), any()))
                .thenReturn("not-a-valid-properties-content=&&!@#");
        // 故意让解析中途失败：温度不是数字
        String badConfig = "spring.ai.ollama.chat.options.temperature=not-a-number";
        when(mockService.getConfigAndSignListener(anyString(), anyString(), anyLong(), any()))
                .thenReturn(badConfig);

        NacosConfigListener listener = new NacosConfigListener(cfg) {
            @Override protected ConfigService createConfigService(Properties props) { return mockService; }
        };
        ReflectionTestUtils.setField(listener, "serverAddr", "127.0.0.1:8848");
        ReflectionTestUtils.setField(listener, "dataId", "x");
        ReflectionTestUtils.setField(listener, "group", "g");

        listener.start(); // 解析失败不应抛

        // 现有字段保留
        assertThat(cfg.isOllamaEnabled()).isTrue();
        assertThat(cfg.getOllamaTemperature()).isEqualTo(0.7);
    }

    @Test
    void startWithBlankUsesDefaultAndNoNacosConnection() {
        AiModelConfig cfg = new AiModelConfig(false, false, "", "", 0.7, "", 0.7);
        NacosConfigListener listener = new NacosConfigListener(cfg);
        ReflectionTestUtils.setField(listener, "serverAddr", null);
        listener.start();
        // 不连 Nacos，cfg 不变
        assertThat(cfg.isOllamaEnabled()).isFalse();
    }
}