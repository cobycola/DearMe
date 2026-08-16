package com.zionysus.dearme.acl.infra.ai.worker;

import com.zionysus.dearme.acl.infra.ai.worker.AiWorkerRequest;

/**
 * AI 调用场景（入参维度）。
 *
 * 业务 adapter 通过 {@link AiWorkerRequest#getScene()} 告知 Router 本次调用属于哪一类业务。
 * Router 按 scene 在注册的 Worker 池里挑一个执行，不在业务层硬编码选哪个模型。
 *
 * 扩展点：未来加新场景（如 AUTHORING、FOLLOWUP、EMBEDDING）在此追加枚举值即可，
 * 业务 adapter 构造 AiWorkerRequest 时填场景，Router 装配层加对应 Worker。
 */
public enum AiScene {
    /** LLM 路由选题 */
    ROUTING,
    /** 报告生成 */
    REPORT
}