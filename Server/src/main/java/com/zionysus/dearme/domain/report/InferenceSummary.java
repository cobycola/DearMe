package com.zionysus.dearme.domain.report;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 推理引擎一次推理结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InferenceSummary {

    private Map<String, Double> probabilities;
    private double entropy;
    private List<Map.Entry<String, Double>> topCandidates;
    private boolean stopRequested;

    public CharacterProfile topCharacter(List<CharacterProfile> candidates) {
        if (topCandidates == null || topCandidates.isEmpty()) {
            return null;
        }
        String topId = topCandidates.get(0).getKey();
        return candidates.stream()
                .filter(c -> c.getId().equals(topId))
                .findFirst()
                .orElse(null);
    }
}