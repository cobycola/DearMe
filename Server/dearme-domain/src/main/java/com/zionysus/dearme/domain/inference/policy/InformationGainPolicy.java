package com.zionysus.dearme.domain.inference.policy;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.inference.engine.Scorer;
import com.zionysus.dearme.domain.inference.engine.WeightedTraitEngine;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息增益选题策略。
 *
 * 对每道未答题模拟 4 个选项作答后的预期熵，取「当前熵 − 期望熵」最大者。
 * 同维度已问题施加指数惩罚避免维度聚集。选项期望熵按等概率 0.25 加权。
 */
@Component
public class InformationGainPolicy implements NextQuestionPolicy {

    private final Scorer scorer;
    private final double sameDimensionPenalty;

    public InformationGainPolicy(
            WeightedTraitEngine engine,
            @Value("${dearme.inference.selector.same-dimension-penalty:0.5}") double sameDimensionPenalty
    ) {
        this.scorer = engine.scorer();
        if (sameDimensionPenalty <= 0 || sameDimensionPenalty > 1) {
            throw new IllegalArgumentException("sameDimensionPenalty 须在 (0,1]");
        }
        this.sameDimensionPenalty = sameDimensionPenalty;
    }

    @Override
    public Question select(List<Question> allQuestions,
                           List<Answer> answered,
                           List<CharacterProfile> candidates,
                           Map<String, Question> questionById) {
        Set<String> answeredIds = new HashSet<>();
        for (Answer answer : answered) {
            answeredIds.add(answer.getQuestionId());
        }

        Map<TraitDimension, Double> currentVec = Scorer.userVector(answered, questionById);
        Map<String, Double> currentProbs = scorer.score(currentVec, candidates);
        double currentEntropy = scorer.entropy(currentProbs);

        // 不再主动停题：按产品定位「agent 不断出题」，题答完才停。
        // 负增益时同维度惩罚 + 仍按 max gain 选负得最小的题，避免选完全无关的题。

        Question best = null;
        double bestGain = Double.NEGATIVE_INFINITY;

        for (Question question : allQuestions) {
            if (answeredIds.contains(question.getId())) {
                continue;
            }

            double expectedEntropy = 0.0;
            for (int opt = 0; opt < 4; opt++) {
                List<Answer> simulated = new ArrayList<>(answered);
                simulated.add(new Answer(question.getId(), opt));
                Map<TraitDimension, Double> simVec = Scorer.userVector(simulated, questionById);
                Map<String, Double> simProbs = scorer.score(simVec, candidates);
                expectedEntropy += 0.25 * scorer.entropy(simProbs);
            }

            double rawGain = currentEntropy - expectedEntropy;

            long sameDimCount = 0;
            for (Answer answer : answered) {
                Question previous = questionById.get(answer.getQuestionId());
                if (previous != null && previous.getDimension() == question.getDimension()) {
                    sameDimCount++;
                }
            }
            double penalty = Math.pow(sameDimensionPenalty, sameDimCount);
            double adjustedGain = rawGain * penalty;

            if (adjustedGain > bestGain) {
                bestGain = adjustedGain;
                best = question;
            }
        }
        return best;
    }
}