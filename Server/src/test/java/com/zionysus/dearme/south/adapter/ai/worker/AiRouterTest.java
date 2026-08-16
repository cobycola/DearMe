package com.zionysus.dearme.south.adapter.ai.worker;

import com.zionysus.dearme.south.adapter.req.AiWorkerRequest;
import com.zionysus.dearme.south.adapter.rsp.AiWorkerResult;
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

        AiRouter router = buildRouter(List.of(routerWorker, reportWorker), true, true);

        AiWorkerRequest<String> request = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(router.execute(request).getValue()).isEqualTo("routed");
        verify(reportWorker, never()).execute(any());
    }

    @Test
    void returnsFailWhenNoWorkerForScene() {
        AiWorker reportOnly = stubWorker(Set.of(AiScene.REPORT));
        AiRouter router = buildRouter(List.of(reportOnly), true, true);

        AiWorkerRequest<String> request = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        AiWorkerResult<String> result = router.execute(request);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getFailureReason()).contains(AiScene.ROUTING.name());
    }

    @Test
    void keepsFirstRegisteredWorkerPerSceneAndDropsDuplicates() {
        AiWorker primary = stubWorker(Set.of(AiScene.ROUTING));
        when(primary.execute(any())).thenReturn(AiWorkerResult.ok("primary"));
        AiWorker dup = stubWorker(Set.of(AiScene.ROUTING)); // 同 scene 第二个

        AiRouter router = buildRouter(List.of(primary, dup), true, true);

        AiWorkerRequest<String> request = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(router.execute(request).getValue()).isEqualTo("primary");
        verify(dup, never()).execute(any());
    }

    @Test
    void skipsWorkerReportingNoScenes() {
        AiWorker empty = stubWorker(Set.of());  // 不报任何场景，应被弃
        AiWorker healthy = stubWorker(Set.of(AiScene.ROUTING));
        when(healthy.execute(any())).thenReturn(AiWorkerResult.ok("ok"));

        AiRouter router = buildRouter(List.of(empty, healthy), true, true);

        AiWorkerRequest<String> request = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(router.execute(request).getValue()).isEqualTo("ok");
    }

    @Test
    void returnsFailWhenWorkerBackendDisabledByConfig() {
        AiWorker ollamaWorker = stubWorker(Set.of(AiScene.ROUTING), "ollama");
        when(ollamaWorker.execute(any())).thenReturn(AiWorkerResult.ok("should-not-reach"));

        // ollama 在 OllamaConfig 里禁用 -> Router 入口过滤,Worker 不被调,返 fail
        AiRouter router = buildRouter(List.of(ollamaWorker), false, true);

        AiWorkerRequest<String> request = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        AiWorkerResult<String> result = router.execute(request);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getFailureReason()).contains("ollama");
        verify(ollamaWorker, never()).execute(any());
    }

    @Test
    void dispatchesWhenWorkerBackendEnabledByConfig() {
        AiWorker ollamaWorker = stubWorker(Set.of(AiScene.ROUTING), "ollama");
        when(ollamaWorker.execute(any())).thenReturn(AiWorkerResult.ok("ok"));

        AiRouter router = buildRouter(List.of(ollamaWorker), true, false); // 仅 ollama 启用

        AiWorkerRequest<String> request = AiWorkerRequest.<String>builder()
                .scene(AiScene.ROUTING).userPrompt("p").outputType(String.class).build();

        assertThat(router.execute(request).getValue()).isEqualTo("ok");
    }

    private static AiRouter buildRouter(List<AiWorker> workers, boolean ollamaEnabled, boolean deepseekEnabled) {
        OllamaConfig ollamaConfig = new OllamaConfig(
                ollamaEnabled, "qwen2.5:7b", "http://localhost:11434", 0.7);
        DeepSeekConfig deepseekConfig = new DeepSeekConfig(deepseekEnabled, "deepseek-v4-flash", 0.7);
        return new AiRouter(workers, ollamaConfig, deepseekConfig);
    }

    private static AiWorker stubWorker(Set<AiScene> scenes) {
        return stubWorker(scenes, null);
    }

    private static AiWorker stubWorker(Set<AiScene> scenes, String backendKey) {
        AiWorker worker = mock(AiWorker.class);
        when(worker.getScenes()).thenReturn(scenes);
        when(worker.getBackendKey()).thenReturn(backendKey);
        return worker;
    }
}
