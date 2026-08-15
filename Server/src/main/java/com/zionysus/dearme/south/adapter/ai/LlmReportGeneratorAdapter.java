package com.zionysus.dearme.south.adapter.ai;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.south.adapter.ai.worker.AiRouter;
import com.zionysus.dearme.south.adapter.ai.worker.AiScene;
import com.zionysus.dearme.south.adapter.ai.worker.AiWorkerRequest;
import com.zionysus.dearme.south.adapter.ai.worker.AiWorkerResult;
import com.zionysus.dearme.south.port.ReportGeneratorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM 报告生成 adapter（南向 AI）。
 *
 * 通过 AiWorker 提交 prompt + ReportDto.class 结构化输出目标。
 * 任何失败（API 错误、解析失败、候选白名单校验失败、超时）→ 降级到 TemplateReportGeneratorAdapter。
 * 降级在 adapter 内部完成，业务层不感知 LLM 失败 —— 保证「付费必得报告」。
 *
 * 直接同进程调用 AiWorker，不走 RPC（ai 子包在本项目内）。
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class LlmReportGeneratorAdapter implements ReportGeneratorPort {

    private final AiRouter aiRouter;
    private final TemplateReportGeneratorAdapter templateFallback;

    @Override
    public String generate(InferenceSummary summary,
                           List<CharacterProfile> candidates,
                           List<Answer> answered,
                           Map<String, Question> questionById) {
        CharacterProfile top = summary.topCharacter(candidates);
        if (top == null) {
            log.warn("推理无 top 候选，直接走降级模板");
            return templateFallback.generate(summary, candidates, answered, questionById);
        }

        String systemPrompt = buildSystemPrompt(candidates);
        String userPrompt = buildUserPrompt(summary, top, candidates, answered, questionById);

        AiWorkerRequest<ReportDto> req = AiWorkerRequest.<ReportDto>builder()
                .scene(AiScene.REPORT)
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .outputType(ReportDto.class)
                .timeoutSeconds(30L)
                .build();

        AiWorkerResult<ReportDto> result = aiRouter.execute(req);
        if (!result.isSuccess()) {
            log.warn("LLM 报告生成失败，走降级模板 reason={}", result.getFailureReason());
            return templateFallback.generate(summary, candidates, answered, questionById);
        }

        ReportDto dto = result.getValue();
        if (!isWhitelisted(dto, candidates)) {
            log.warn("LLM 返回候选不再白名单，走降级模板 matchedId={}", dto.getMatchedCharacterId());
            return templateFallback.generate(summary, candidates, answered, questionById);
        }

        return renderMarkdown(dto);
    }

    private boolean isWhitelisted(ReportDto dto, List<CharacterProfile> candidates) {
        if (dto == null || dto.getMatchedCharacterId() == null) {
            return false;
        }
        return candidates.stream().anyMatch(c -> c.getId().equals(dto.getMatchedCharacterId()));
    }

    private String buildSystemPrompt(List<CharacterProfile> candidates) {
        return """
                你是一位资深心理画像分析师，擅长通过定制问卷定制化背包出题结果为用户做人物画像推理报告。
                铁律：
                1. 你必须只用用户已答到的真实信息推理，不得编造未涉及的维度内容。
                2. matchedCharacterId 必须严格使用候选列表里的某个 id 原值，不得自估。
                3. matchedCharacterName 必须是上述候选的 name 原值。
                4. analysis 用第二人称，读起来像「你」而不是「用户」。
                5. 三句之内点明你核心特质，随后说明为什么像这个角色。
                6. advice 部分 2~3 句，给出一项具体可操作的建议。
                7. 全文语气真诚、不广式、非参比。避免使用「从某些方面」「在某些意义上」这类広式术语。

                全部可选候选（id｜name｜原型｜一句话人设）：
                """ + candidates.stream()
                .map(c -> c.getId() + "｜" + c.getName() + "｜" + c.getArchetype() + "｜" + c.getBlurb())
                .collect(Collectors.joining("\n"));
    }

    private String buildUserPrompt(InferenceSummary summary,
                                   CharacterProfile top,
                                   List<CharacterProfile> candidates,
                                   List<Answer> answered,
                                   Map<String, Question> questionById) {
        StringBuilder sb = new StringBuilder();
        sb.append("推理系统已基于以下答题结果得出候选分布，请生成报告。\n\n");
        sb.append("候选 Top-5（id: 概率）：\n");
        int max = Math.min(5, summary.getTopCandidates().size());
        for (int i = 0; i < max; i++) {
            var e = summary.getTopCandidates().get(i);
            String name = candidates.stream()
                    .filter(c -> c.getId().equals(e.getKey()))
                    .map(CharacterProfile::getName)
                    .findFirst()
                    .orElse(e.getKey());
            sb.append("- ").append(e.getKey()).append(" (").append(name).append("): ")
                    .append(String.format("%.1f%%", e.getValue() * 100)).append("\n");
        }
        sb.append("\n系统认定 Top-1 候选：").append(top.getId()).append(" (").append(top.getName()).append(")\n\n");

        sb.append("用户已答题目（题面｜选中选项）：\n");
        for (Answer a : answered) {
            Question q = questionById.get(a.getQuestionId());
            if (q == null) {
                continue;
            }
            sb.append("- ").append(q.getPrompt()).append(" → ")
                    .append(q.getOptions().get(a.getOptionIndex()))
                    .append("【维度: ").append(q.getDimension()).append("】\n");
        }

        sb.append("\n用户维度倾向（仅基于已答）：\n");
        Map<TraitDimension, Double> vec = collectUserVector(answered, questionById);
        vec.forEach((d, v) -> sb.append("- ").append(d).append(": ")
                .append(String.format("%.2f", v)).append("\n"));

        sb.append("""

                请输出包含以下字段的结构化报告：
                - matchedCharacterId（务必从候选 id 取原值）
                - matchedCharacterName
                - headline（一句话核心画像，不超过 30 字）
                - analysis（三段以内的深度分析，说明为什么是这个角色）
                - advice（2~3 句具体可操作的建议）
                """);
        return sb.toString();
    }

    private String renderMarkdown(ReportDto dto) {
        return new StringBuilder()
                .append("# ").append(dto.getHeadline()).append("\n\n")
                .append("## 你最像：").append(dto.getMatchedCharacterName()).append("\n\n")
                .append("## 画像分析\n").append(dto.getAnalysis()).append("\n\n")
                .append("## 给你的建议\n").append(dto.getAdvice()).append("\n")
                .toString();
    }

    private static Map<TraitDimension, Double> collectUserVector(List<Answer> answered, Map<String, Question> questionById) {
        Map<TraitDimension, double[]> acc = new java.util.HashMap<>();
        for (Answer a : answered) {
            Question q = questionById.get(a.getQuestionId());
            if (q == null) {
                continue;
            }
            double off = q.optionOffset(a.getOptionIndex());
            double[] cur = acc.computeIfAbsent(q.getDimension(), k -> new double[]{0, 0});
            cur[0] += off;
            cur[1] += 1;
        }
        Map<TraitDimension, Double> result = new java.util.EnumMap<>(TraitDimension.class);
        acc.forEach((d, arr) -> result.put(d, arr[1] > 0 ? arr[0] / arr[1] : 0));
        return result;
    }
}