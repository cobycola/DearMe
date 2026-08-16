package com.zionysus.dearme.south.adapter.ai;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.south.adapter.ai.worker.AiRouter;
import com.zionysus.dearme.south.adapter.rsp.AiWorkerResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmReportGeneratorAdapterTest {

    private final TemplateReportGeneratorAdapter template = new TemplateReportGeneratorAdapter();

    @Test
    void shouldRenderMarkdownWhenLlmReturnsWhitelistedChar() {
        AiRouter router = mock(AiRouter.class);
        when(router.execute(any())).thenReturn(AiWorkerResult.ok(new ReportDto(
                "alice", "爱丽丝", "你是个梦游仙境的兔子",
                "分析：你的好奇与她共振。", "建议：抓住每一次来不及的灵感。"
        )));
        LlmReportGeneratorAdapter adapter = new LlmReportGeneratorAdapter(router, template);

        CharacterProfile alice = profile("alice", 0.8, 0.0);
        InferenceSummary summary = new InferenceSummary(
                Map.of("alice", 1.0), 0.0, List.of(Map.entry("alice", 1.0)), false);

        String md = adapter.generate(summary, List.of(alice), List.of(),
                Map.of());

        assertThat(md).contains("你是个梦游仙境的兔子");
        assertThat(md).contains("分析：你的好奇与她共振");
    }

    @Test
    void shouldFallbackToTemplateWhenLlmReturnsNonWhitelistedChar() {
        AiRouter router = mock(AiRouter.class);
        when(router.execute(any())).thenReturn(AiWorkerResult.ok(new ReportDto(
                "ghost", "鬼魂", "head", "analysis", "advice"  // ghost 不在白名单
        )));
        LlmReportGeneratorAdapter adapter = new LlmReportGeneratorAdapter(router, template);

        CharacterProfile alice = profile("alice", 0.8, 0.0);
        InferenceSummary summary = new InferenceSummary(
                Map.of("alice", 1.0), 0.0, List.of(Map.entry("alice", 1.0)), false);

        String md = adapter.generate(summary, List.of(alice), List.of(), Map.of());

        // 应该走降级模板：包含「你最像：爱丽丝」格式
        assertThat(md).contains("你最像：爱丽丝");
        assertThat(md).doesNotContain("鬼魂");
    }

    @Test
    void shouldFallbackToTemplateWhenLlmFails() {
        AiRouter router = mock(AiRouter.class);
        when(router.execute(any())).thenReturn(AiWorkerResult.fail("LLM timeout"));
        LlmReportGeneratorAdapter adapter = new LlmReportGeneratorAdapter(router, template);

        CharacterProfile alice = profile("alice", 0.8, 0.0);
        InferenceSummary summary = new InferenceSummary(
                Map.of("alice", 1.0), 0.0, List.of(Map.entry("alice", 1.0)), false);

        String md = adapter.generate(summary, List.of(alice), List.of(), Map.of());

        // 付费必得报告：宁可走模板也不能空返回
        assertThat(md).isNotBlank();
        assertThat(md).contains("你最像：爱丽丝");
    }

    @Test
    void shouldFallbackToTemplateWhenInferenceHasNoTopCandidate() {
        // 推理无 top 候选：topCharacter 返回 null
        InferenceSummary summary = new InferenceSummary(
                Map.of(), 1.0, List.of(), false);

        AiRouter router = mock(AiRouter.class);
        LlmReportGeneratorAdapter adapter = new LlmReportGeneratorAdapter(router, template);

        assertThat(adapter.generate(summary, List.of(), List.of(), Map.of()))
                .contains("暂时无法生成报告");
    }

    private static CharacterProfile profile(String id, double v1, double v2) {
        Map<TraitDimension, Double> t = new HashMap<>();
        t.put(TraitDimension.INTROVERT_EXTROVERT, v1);
        t.put(TraitDimension.IDEALIST_PRAGMATIST, v2);
        return CharacterProfile.builder().id(id).name(translate(id)).source("test").archetype("test").blurb("test").traits(t).build();
    }

    private static String translate(String id) {
        return "alice".equals(id) ? "爱丽丝" : id;
    }
}