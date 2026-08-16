package com.zionysus.dearme.domain.inference.engine;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 加权特征匹配推理引擎。复用 {@link Scorer} 的高斯核相似度。
 *
 * 通过显式 new Scorer(temperature) 而非 @Component Scorer，
 * 以保持 Scorer 纯函数、无 Spring 依赖，便于脱离容器单测。
 */
@Component
public class WeightedTraitEngine implements InferenceEngine {

    private final Scorer scorer;

    public WeightedTraitEngine(@Value("${dearme.inference.scorer.temperature:8.0}") double temperature) {
        this.scorer = new Scorer(temperature);
    }

    @Override
    public InferenceSummary infer(List<Answer> answers,
                                  List<CharacterProfile> candidates,
                                  Map<String, Question> questionById) {
        Map<TraitDimension, Double> userVec = Scorer.userVector(answers, questionById);
        Map<String, Double> probs = scorer.score(userVec, candidates);
        double entropy = scorer.entropy(probs);
        List<Map.Entry<String, Double>> top = scorer.topN(probs, Math.min(5, candidates.size()));
        return new InferenceSummary(probs, entropy, top, false);
    }

    public Scorer scorer() {
        return scorer;
    }
}