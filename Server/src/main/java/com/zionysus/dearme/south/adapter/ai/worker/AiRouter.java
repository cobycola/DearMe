package com.zionysus.dearme.south.adapter.ai.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI Worker 分发器（Router 策略池核心）。
 *
 * 职责：按 {@link AiWorkerRequest#getScene()} 在 SPRING 注册的 Worker 里挑一个执行。
 *   - 不重试、不主备切换、不做降级 —— 降级是业务 adapter 的语义（沿用 D2 模式）
 *   - 装配时一 scene 一票：同 scene 多个 Worker 启用时取其一，余者记录日志丢弃
 *   - 装配时无 Worker 的 scene 不视为启动错误，但 execute 该 scene 会返回 fail
 *     （让业务 adapter 走自己的降级路径，而不是 Spring 启动直接挂）
 *
 * 扩展点：未来加新模型，新增 AiWorker 实现并 @Component 即被本类自动装配，不影响业务 adapter。
 */
@Slf4j
@Component
public class AiRouter {

    private final Map<AiScene, AiWorker> workers;
    private final AiModelConfig aiModelConfig;

    public AiRouter(List<AiWorker> all, AiModelConfig aiModelConfig) {
        this.aiModelConfig = aiModelConfig;
        Map<AiScene, AiWorker> map = new EnumMap<>(AiScene.class);
        for (AiWorker w : all) {
            Set<AiScene> scenes = w.getScenes();
            if (scenes == null || scenes.isEmpty()) {
                log.warn("[AiRouter] Worker {} 不报任何场景，弃用", w.getClass().getSimpleName());
                continue;
            }
            for (AiScene scene : scenes) {
                AiWorker prev = map.putIfAbsent(scene, w);
                if (prev != null) {
                    log.warn("[AiRouter] 场景 {} 已有 Worker {}，新 Worker {} 被丢弃（一 scene 一票）",
                            scene, prev.getClass().getSimpleName(), w.getClass().getSimpleName());
                }
            }
        }
        this.workers = map;
        log.info("[AiRouter] 装配完成 scenes={}", map.keySet());
    }

    public <T> AiWorkerResult<T> execute(AiWorkerRequest<T> request) {
        AiScene scene = request.getScene();
        AiWorker worker = workers.get(scene);
        if (worker == null) {
            return AiWorkerResult.fail("无可用 Worker 支持场景 " + scene + "（业务 adapter 应走降级）");
        }
        String backend = worker.getBackendKey();
        if (backend != null && !aiModelConfig.isBackendEnabled(backend)) {
            return AiWorkerResult.fail("backend " + backend + " 当前禁用，业务 adapter 应走降级");
        }
        return worker.execute(request);
    }
}