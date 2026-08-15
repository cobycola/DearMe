package com.zionysus.dearme.south.adapter.ai.worker;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Worker 统一调用入参。
 *
 * 抽象层屏蔽底层 ChatClient 的细节：
 *   - 业务 adapter 只负责构造 prompt + 指定输出类型
 *   - worker 实现负责执行 prompt、调 LLM、强转结构化输出
 *
 * @param <T> 结构化输出目标类型
 */
@Data
@NoArgsConstructor
@Builder
public class AiWorkerRequest<T> {

    private AiScene scene;
    private String systemPrompt;
    private String userPrompt;
    private Class<T> outputType;
    private long timeoutSeconds;

    public AiWorkerRequest(AiScene scene, String systemPrompt, String userPrompt, Class<T> outputType, long timeoutSeconds) {
        if (scene == null) {
            throw new IllegalArgumentException("scene 必填，Router 按 scene 分派 Worker");
        }
        if (outputType == null) {
            throw new IllegalArgumentException("outputType 必填，AiWorker 走结构化输出");
        }
        this.scene = scene;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.outputType = outputType;
        this.timeoutSeconds = timeoutSeconds;
    }
}