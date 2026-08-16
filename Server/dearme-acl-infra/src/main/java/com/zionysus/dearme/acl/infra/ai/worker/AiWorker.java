package com.zionysus.dearme.acl.infra.ai.worker;

import com.zionysus.dearme.acl.infra.ai.worker.AiWorkerRequest;
import com.zionysus.dearme.acl.infra.ai.worker.AiWorkerResult;

import java.util.Set;

/**
 * AI Worker 统一抽象（南向 AI 网关的核心）。
 *
 * 所有需要调 LLM 的业务 adapter（报告生成、动态路由选题等）
 * 都应通过 {@link AiRouter} 而非直接调具体模型实现。
 *
 * 扩展点：
 *   - 当前 ChatClientAiWorker：基于 Spring AI ChatClient.entity() 结构化输出（deepseek）
 *   - OllamaWorker：本地小模型 CPU 跑（零边际成本）
 *   - 未来可加 OpenAiWorker 等，新实现此接口并在 getScenes() 自报擅长场景
 *
 * 语义：每个 Worker 通过 {@link #getScenes()} 自报它支持的 {@link AiScene} 集合，
 *      Router 按 request.scene 在已注册 Worker 池中挑一个执行。
 *      Worker 自行吞异常转 AiWorkerResult.fail；Router 不重试不降级，降级由业务 adapter 各自处理。
 */
public interface AiWorker {

    /** 此 Worker 支持的场景集合。Router 按场景装配时读它。 */
    Set<AiScene> getScenes();

    /** 此 Worker 所属的后端 key（如 "ollama" / "deepseek"），Router 入口按 OllamaConfig/DeepSeekConfig 启用状态过滤用。null 表示后端无关。 */
    default String getBackendKey() { return null; }

    <T> AiWorkerResult<T> execute(AiWorkerRequest<T> request);
}