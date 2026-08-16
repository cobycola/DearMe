package com.zionysus.dearme.domain.inference.engine;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 候选打分器（纯函数）。
 *
 * 算法：用户向量与各候选维度向量做高斯核相似度
 *   raw_i = exp(-temperature * ‖u - v_i‖²)
 *   p_i = raw_i / Σ raw
 *
 * temperature 控制分布锐度。分布越集中熵越低 = 推理越收敛。
 */
public class Scorer {

    private final double temperature;

    public Scorer(double temperature) {
        if (temperature <= 0) {
            throw new IllegalArgumentException("temperature 必须 > 0");
        }
        this.temperature = temperature;
    }

    public double temperature() {
        return temperature;
    }

    public Map<String, Double> score(Map<TraitDimension, Double> userVector, List<CharacterProfile> candidates) {
        Map<String, Double> raw = new HashMap<>();
        double sum = 0.0;
        for (CharacterProfile candidate : candidates) {
            double distSq = squaredDistance(userVector, candidate);
            double score = Math.exp(-temperature * distSq);
            raw.put(candidate.getId(), score);
            sum += score;
        }
        // TreeMap 保证测试断言顺序稳定
        Map<String, Double> probabilities = new TreeMap<>();
        if (sum <= 0) {
            double equi = 1.0 / candidates.size();
            for (CharacterProfile candidate : candidates) {
                probabilities.put(candidate.getId(), equi);
            }
            return probabilities;
        }
        for (Map.Entry<String, Double> entry : raw.entrySet()) {
            probabilities.put(entry.getKey(), entry.getValue() / sum);
        }
        return probabilities;
    }

    public double entropy(Map<String, Double> probabilities) {
        double h = 0.0;
        for (double value : probabilities.values()) {
            if (value > 0) {
                h -= value * Math.log(value);
            }
        }
        return h;
    }

    public List<Map.Entry<String, Double>> topN(Map<String, Double> probabilities, int n) {
        return probabilities.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .toList();
    }

    private double squaredDistance(Map<TraitDimension, Double> userVector, CharacterProfile candidate) {
        double sum = 0.0;
        for (TraitDimension dimension : TraitDimension.values()) {
            double userValue = userVector.getOrDefault(dimension, 0.0);
            double candidateValue = candidate.trait(dimension);
            double diff = userValue - candidateValue;
            sum += diff * diff;
        }
        return sum;
    }

    /**
     * 从答题记录算出用户当前维度向量。每维度 = 该维度已答题选项偏移的均值。
     * 未答维度视为 0（中立）。
     */
    public static Map<TraitDimension, Double> userVector(List<Answer> answers, Map<String, Question> questionById) {
        Map<TraitDimension, List<Double>> collected = new EnumMap<>(TraitDimension.class);
        for (Answer answer : answers) {
            Question question = questionById.get(answer.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("找不到题目: " + answer.getQuestionId());
            }
            double offset = question.optionOffset(answer.getOptionIndex());
            collected.computeIfAbsent(question.getDimension(), k -> new ArrayList<>()).add(offset);
        }
        Map<TraitDimension, Double> result = new EnumMap<>(TraitDimension.class);
        for (Map.Entry<TraitDimension, List<Double>> entry : collected.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            result.put(entry.getKey(), avg);
        }
        return result;
    }
}
