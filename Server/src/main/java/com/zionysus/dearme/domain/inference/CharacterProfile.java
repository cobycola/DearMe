package com.zionysus.dearme.domain.inference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 候选人物画像。维度值范围 [-1.0, +1.0]。缺失维度视为 0（中立）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterProfile {

    private String id;
    private String name;
    private String source;
    private String archetype;
    private String blurb;
    private Map<TraitDimension, Double> traits;

    public double trait(TraitDimension dim) {
        if (traits == null) {
            return 0.0;
        }
        return traits.getOrDefault(dim, 0.0);
    }
}