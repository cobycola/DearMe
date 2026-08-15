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
        for (CharacterProfile c : candidates) {
            double distSq = squaredDistance(userVector, c);
            double r = Math.exp(-temperature * distSq);
            raw.put(c.getId(), r);
            sum += r;
        }
        // TreeMap 保证测试断言顺序稳定
        Map<String, Double> p = new TreeMap<>();
        if (sum <= 0) {
            double equi = 1.0 / candidates.size();
            for (CharacterProfile c : candidates) {
                p.put(c.getId(), equi);
            }
            return p;
        }
        for (Map.Entry<String, Double> e : raw.entrySet()) {
            p.put(e.getKey(), e.getValue() / sum);
        }
        return p;
    }

    public double entropy(Map<String, Double> probabilities) {
        double h = 0.0;
        for (double v : probabilities.values()) {
            if (v > 0) {
                h -= v * Math.log(v);
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

    private double squaredDistance(Map<TraitDimension, Double> userVector, CharacterProfile c) {
        double sum = 0.0;
        for (TraitDimension d : TraitDimension.values()) {
            double u = userVector.getOrDefault(d, 0.0);
            double v = c.trait(d);
            double diff = u - v;
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
        for (Answer a : answers) {
            Question q = questionById.get(a.getQuestionId());
            if (q == null) {
                throw new IllegalArgumentException("找不到题目: " + a.getQuestionId());
            }
            double offset = q.optionOffset(a.getOptionIndex());
            collected.computeIfAbsent(q.getDimension(), k -> new ArrayList<>()).add(offset);
        }
        Map<TraitDimension, Double> result = new EnumMap<>(TraitDimension.class);
        for (Map.Entry<TraitDimension, List<Double>> e : collected.entrySet()) {
            double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            result.put(e.getKey(), avg);
        }
        return result;
    }
}