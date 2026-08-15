package com.zionysus.dearme.south.adapter.ai.worker;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiRouterTest {

    @Test
    void routesToWorkerRegisteredForThatScene() {
        AiWorker routerWorker = stubWorker(Set.of(AiScene.ROUTING));
        when(routerWorker.execute(any())).thenReturn(AiWorkerResult.ok("routed"));
        AiWorker reportWorker = stubWorker(Set.of(AiScene.REPORT));
        when(reportWorker.execute(any())).thenReturn(AiWorkerResult.ok("reported"));

        AiRouter r = buildRouter(List.of(routerWorker, reportWorker), true, true);

        AiWorkerRequest<String> req = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(r.execute(req).getValue()).isEqualTo("routed");
        verify(reportWorker, never()).execute(any());
    }

    @Test
    void returnsFailWhenNoWorkerForScene() {
        AiWorker reportOnly = stubWorker(Set.of(AiScene.REPORT));
        AiRouter r = buildRouter(List.of(reportOnly), true, true);

        AiWorkerRequest<String> req = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        AiWorkerResult<String> res = r.execute(req);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getFailureReason()).contains(AiScene.ROUTING.name());
    }

    @Test
    void keepsFirstRegisteredWorkerPerSceneAndDropsDuplicates() {
        AiWorker primary = stubWorker(Set.of(AiScene.ROUTING));
        when(primary.execute(any())).thenReturn(AiWorkerResult.ok("primary"));
        AiWorker dup = stubWorker(Set.of(AiScene.ROUTING)); // 同 scene 第二个

        AiRouter r = buildRouter(List.of(primary, dup), true, true);

        AiWorkerRequest<String> req = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(r.execute(req).getValue()).isEqualTo("primary");
        verify(dup, never()).execute(any());
    }

    @Test
    void skipsWorkerReportingNoScenes() {
        AiWorker empty = stubWorker(Set.of());  // 不报任何场景，应被弃
        AiWorker healthy = stubWorker(Set.of(AiScene.ROUTING));
        when(healthy.execute(any())).thenReturn(AiWorkerResult.ok("ok"));

        AiRouter r = buildRouter(List.of(empty, healthy), true, true);

        AiWorkerRequest<String> req = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(r.execute(req).getValue()).isEqualTo("ok");
    }

    @Test
    void returnsFailWhenWorkerBackendDisabledByAiModelConfig() {
        AiWorker ollamaWorker = stubWorker(Set.of(AiScene.ROUTING), "ollama");
        when(ollamaWorker.execute(any())).thenReturn(AiWorkerResult.ok("should-not-reach"));

        // ollama 在 AiModelConfig 里禁用 -> Router 入口过滤,Worker 不被调,返 fail
        AiRouter r = buildRouter(List.of(ollamaWorker), false, true);

        AiWorkerRequest<String> req = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        AiWorkerResult<String> res = r.execute(req);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getFailureReason()).contains("ollama");
        verify(ollamaWorker, never()).execute(any());
    }

    @Test
    void dispatchesWhenWorkerBackendEnabledByAiModelConfig() {
        AiWorker ollamaWorker = stubWorker(Set.of(AiScene.ROUTING), "ollama");
        when(ollamaWorker.execute(any())).thenReturn(AiWorkerResult.ok("ok"));

        AiRouter r = buildRouter(List.of(ollamaWorker), true, false); // 仅 ollama 启用

        AiWorkerRequest<String> req = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(r.execute(req).getValue()).isEqualTo("ok");
    }

    private static AiRouter buildRouter(List<AiWorker> workers, boolean ollamaEnabled, boolean deepseekEnabled) {
        AiModelConfig cfg = new AiModelConfig(
                ollamaEnabled, deepseekEnabled,
                "qwen2.5:7b", "http://localhost:11434", 0.7,
                "deepseek-v4-flash", 0.7);
        return new AiRouter(workers, cfg);
    }

    private static AiWorker stubWorker(Set<AiScene> scenes) {
        return stubWorker(scenes, null);
    }

    private static AiWorker stubWorker(Set<AiScene> scenes, String backendKey) {
        AiWorker m = mock(AiWorker.class);
        when(m.getScenes()).thenReturn(scenes);
        when(m.getBackendKey()).thenReturn(backendKey);
        return m;
    }
}