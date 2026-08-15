package com.zionysus.dearme.south.adapter.ai.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 本地 Ollama 小模型 Worker（CPU 跑，零边际成本）。
 *
 * 装配条件：dearme.ai.backends.ollama.enabled=true（默认不开，避免无 Ollama 守护进程时空指针）。
 *           启动方式：先本机 ollama serve + ollama pull <model>，
 *           再 SPRING_PROFILES_ACTIVE=ollama 启 Spring Boot（application-ollama.properties 配 base-url）。
 *
 * 实现：直接注入 OllamaChatModel 自建 ChatClient（避免与 deepseek starter 同在 classpath 时
 *      ChatClient.Builder 候选冲突），后续路径与 ChatClientAiWorker 同款 .entity() 结构化输出。
 *      失败一律吞异常转 AiWorkerResult.fail，业务 adapter 各自降级（沿用 D2 模式）。
 *
 * 与 D2 关系：本地小模型也走结构化输出 entity()，7B 量化模型抽风率不低，
 *           失败后业务 adapter 自行降级（路由 → InformationGainPolicy；报告 → 模板 adapter）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "dearme.ai.backends.ollama.enabled", havingValue = "true")
public class OllamaWorker implements AiWorker {

    private final ChatClient chatClient;

    public OllamaWorker(OllamaChatModel ollamaChatModel) {
        this.chatClient = ChatClient.builder(ollamaChatModel).build();
    }

    @Override
    public Set<AiScene> getScenes() {
        return Set.of(AiScene.ROUTING, AiScene.REPORT);
    }

    @Override
    public String getBackendKey() { return "ollama"; }

    @Override
    public <T> AiWorkerResult<T> execute(AiWorkerRequest<T> request) {
        try {
            ChatClient.ChatClientRequestSpec spec = chatClient.prompt();
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
                spec = spec.system(request.getSystemPrompt());
            }
            if (request.getUserPrompt() == null || request.getUserPrompt().isBlank()) {
                return AiWorkerResult.fail("userPrompt 不能为空");
            }
            T result = spec.user(request.getUserPrompt())
                    .call()
                    .entity(request.getOutputType());
            return AiWorkerResult.ok(result);
        } catch (Exception e) {
            log.warn("[OllamaWorker] 执行失败 outputType={}, reason={}", request.getOutputType().getSimpleName(), e.getMessage());
            return AiWorkerResult.fail(e.getMessage());
        }
    }
}