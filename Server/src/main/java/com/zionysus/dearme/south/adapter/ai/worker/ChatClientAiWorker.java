package com.zionysus.dearme.south.adapter.ai.worker;

import com.zionysus.dearme.south.adapter.req.AiWorkerRequest;
import com.zionysus.dearme.south.adapter.rsp.AiWorkerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 基于 Spring AI ChatClient 的 AiWorker 实现（deepseek 远程网关）。
 *
 * 关键设计：
 *   - 所有失败（含超时/网络/解析/模型异常）都吞掉，转为 AiWorkerResult.fail(reason)
 *     让业务 adapter 自己决定降级路径，控制流不被异常打断
 *   - timeout 由配置控制（application.properties）；本类层面不强加超时实现，
 *     Spring AI 自带的 retry 配置 + HTTP 超时为准
 *   - 直接同进程调用，不走 RPC
 *
 * 装配：默认关闭（踩 CLAUDE.md 红线「严禁烧钱测 LLM」）。deepseek 真付费上生产时
 *      应用启动加 dearme.ai.backends.deepseek.enabled=true 启用。
 *      本地 Ollama 启用时（dearme.ai.backends.ollama.enabled=true），本 Worker 不装配，
 *      Router 自行选 OllamaWorker。
 *
 * 扩展点：未来加 OpenAiWorker、OllamaWorker 时，新实现 AiWorker 接口，不改业务 adapter。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "dearme.ai.backends.deepseek.enabled", havingValue = "true")
public class ChatClientAiWorker implements AiWorker {

    private final ChatClient chatClient;

    public ChatClientAiWorker(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Set<AiScene> getScenes() {
        return Set.of(AiScene.REPORT);
    }

    @Override
    public String getBackendKey() { return "deepseek"; }

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
            log.warn("AiWorker 执行失败 outputType={}, reason={}", request.getOutputType().getSimpleName(), e.getMessage());
            return AiWorkerResult.fail(e.getMessage());
        }
    }
}