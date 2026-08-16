package com.zionysus.dearme.domain.inference.engine;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScorerTest {

    private final Scorer scorer = new Scorer(8.0);

    @Test
    void shouldRankCandidateClosestToUserFirst() {
        CharacterProfile alice = profile("alice", 0.8, 0.0);
        CharacterProfile bob = profile("bob", -0.8, 0.0);
        CharacterProfile cara = profile("cara", 0.1, 0.0);
        Map<String, Double> probs = scorer.score(vec(0.7, 0.0), List.of(alice, bob, cara));

        assertThat(scorer.topN(probs, 1).get(0).getKey()).isEqualTo("alice");
        assertThat(probs.get("alice")).isGreaterThan(probs.get("bob"));
        // 概率归一化保证和为 1
        assertThat(probs.values().stream().mapToDouble(Double::doubleValue).sum())
                .isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void emptyAnswerVectorShouldYieldAlmostEqualProbabilities() {
        CharacterProfile alice = profile("alice", 0.8, 0.0);
        CharacterProfile bob = profile("bob", -0.8, 0.0);
        Map<String, Double> probs = scorer.score(vec(0.0, 0.0), List.of(alice, bob));

        // 用户向量全 0 时，距离差仅由候选差异决定，bob 远于 alice（向量距离一致）
        // 实际两候选到原点的距离都是 0.8，所以概率应当完全相等
        assertThat(probs.get("alice")).isCloseTo(probs.get("bob"),
                org.assertj.core.data.Offset.offset(1e-9));
        assertThat(scorer.entropy(probs)).isCloseTo(Math.log(2),
                org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    void highTemperatureSharpensDistribution() {
        CharacterProfile alice = profile("alice", 0.9, 0.0);
        CharacterProfile bob = profile("bob", 0.85, 0.0);
        Scorer sharp = new Scorer(50.0);
        Scorer flat = new Scorer(1.0);

        Map<String, Double> sharpProbs = sharp.score(vec(0.9, 0.0), List.of(alice, bob));
        Map<String, Double> flatProbs = flat.score(vec(0.9, 0.0), List.of(alice, bob));

        // 高温下 alice（恰好命中用户向量）相对 bob 的概率比应更陡
        double sharpRatio = sharpProbs.get("alice") / sharpProbs.get("bob");
        double flatRatio = flatProbs.get("alice") / flatProbs.get("bob");
        assertThat(sharpRatio).isGreaterThan(flatRatio);
    }

    @Test
    void topNReturnsInDescendingOrder() {
        CharacterProfile a = profile("a", 0.9, 0.0);
        CharacterProfile b = profile("b", 0.0, 0.0);
        CharacterProfile c = profile("c", -0.9, 0.0);
        Map<String, Double> probs = scorer.score(vec(0.95, 0.0), List.of(a, b, c));
        List<Map.Entry<String, Double>> top = scorer.topN(probs, 2);
        assertThat(top).hasSize(2);
        assertThat(top.get(0).getValue()).isGreaterThanOrEqualTo(top.get(1).getValue());
        assertThat(top.get(0).getKey()).isEqualTo("a");
    }

    private static CharacterProfile profile(String id, double dim1, double dim2) {
        Map<TraitDimension, Double> t = new HashMap<>();
        t.put(TraitDimension.INTROVERT_EXTROVERT, dim1);
        t.put(TraitDimension.IDEALIST_PRAGMATIST, dim2);
        return CharacterProfile.builder().id(id).name(id).source("test").archetype("test").blurb("test").traits(t).build();
    }

    private static Map<TraitDimension, Double> vec(double v1, double v2) {
        Map<TraitDimension, Double> v = new HashMap<>();
        v.put(TraitDimension.INTROVERT_EXTROVERT, v1);
        v.put(TraitDimension.IDEALIST_PRAGMATIST, v2);
        return v;
    }
}