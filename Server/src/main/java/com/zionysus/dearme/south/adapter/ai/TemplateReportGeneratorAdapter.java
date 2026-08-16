package com.zionysus.dearme.south.adapter.ai;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.south.port.ReportGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 模板报告 adapter（降级底线）。
 *
 * 当 LLM adapter 失败时降级用，保证「付费必得报告」。
 * 也实现 ReportGeneratorPort，便于测试与小规模回退。
 */
@Component
public class TemplateReportGeneratorAdapter implements ReportGeneratorPort {

    @Override
    public String generate(InferenceSummary summary,
                           List<CharacterProfile> candidates,
                           List<Answer> answered,
                           Map<String, Question> questionById) {
        CharacterProfile top = summary.topCharacter(candidates);
        if (top == null) {
            return "暂时无法生成报告，请稍后重试。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# 你最像：").append(top.getName()).append("\n\n");
        sb.append("> ").append(top.getSource()).append(" · ").append(top.getArchetype()).append("\n\n");
        sb.append("## 一句话画像\n").append(top.getBlurb()).append("\n\n");

        sb.append("## 推理依据\n");
        sb.append("基于 ").append(answered.size()).append(" 轮定制问卷，");
        sb.append("我们在 ").append(candidates.size()).append(" 位候选中定位到你的最佳匹配是「")
                .append(top.getName()).append("」。\n\n");

        if (!summary.getTopCandidates().isEmpty()) {
            sb.append("## 候选分布 Top-3\n");
            int max = Math.min(3, summary.getTopCandidates().size());
            for (int i = 0; i < max; i++) {
                Map.Entry<String, Double> entry = summary.getTopCandidates().get(i);
                String name = candidates.stream()
                        .filter(c -> c.getId().equals(entry.getKey()))
                        .map(CharacterProfile::getName)
                        .findFirst()
                        .orElse(entry.getKey());
                sb.append(String.format("%d. %s — %.1f%%\n", i + 1, name, entry.getValue() * 100));
            }
            sb.append("\n");
        }

        // 列出几个最显著的维度倾向
        sb.append("## 关键倾向\n");
        Map<TraitDimension, Double> userVec = collectUserVector(answered, questionById);
        userVec.entrySet().stream()
                .sorted((a, b) -> Double.compare(Math.abs(b.getValue()), Math.abs(a.getValue())))
                .limit(3)
                .forEach(e -> sb.append("- ").append(describe(e.getKey(), e.getValue())).append("\n"));
        sb.append("\n");

        sb.append("---\n（※ 此报告由模板生成；LLM 报告未就绪时兜底使用）");
        return sb.toString();
    }

    private static String describe(TraitDimension dimension, double value) {
        String trend = value > 0 ? "偏「" + dimension.name().substring(dimension.name().indexOf('_') + 1) + "」"
                : "偏「" + dimension.name().substring(0, dimension.name().indexOf('_')) + "」";
        return dimension.name() + " " + trend + " " + String.format("%.2f", Math.abs(value));
    }

    private static Map<TraitDimension, Double> collectUserVector(List<Answer> answered, Map<String, Question> questionById) {
        Map<TraitDimension, double[]> acc = new java.util.HashMap<>();
        for (Answer answer : answered) {
            Question question = questionById.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }
            double off = question.optionOffset(answer.getOptionIndex());
            double[] cur = acc.computeIfAbsent(question.getDimension(), k -> new double[]{0, 0});
            cur[0] += off;
            cur[1] += 1;
        }
        Map<TraitDimension, Double> result = new java.util.EnumMap<>(TraitDimension.class);
        acc.forEach((d, arr) -> result.put(d, arr[1] > 0 ? arr[0] / arr[1] : 0));
        return result;
    }
}