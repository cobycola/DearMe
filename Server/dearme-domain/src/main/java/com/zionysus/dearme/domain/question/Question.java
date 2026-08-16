package com.zionysus.dearme.domain.question;

import com.zionysus.dearme.domain.inference.TraitDimension;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单选量表题。四个选项分别对应绑定维度的 [-0.75, -0.25, +0.25, +0.75] 强度偏移。
 *
 * 业务构造走全参 {@link #Question(String, String, TraitDimension, List, int)}，带四个选项校验。
 * Jackson 反序列化走无参 + setter，options setter 同样带 4 个选项校验，避免脏数据流入。
 */
@Data
@NoArgsConstructor
@Builder
public class Question {

    private String id;
    private String prompt;
    private TraitDimension dimension;
    private List<String> options;
    private int fewerInfoOptionIndex;

    public Question(String id, String prompt, TraitDimension dimension,
                    List<String> options, int fewerInfoOptionIndex) {
        requireValidOptions(id, options);
        this.id = id;
        this.prompt = prompt;
        this.dimension = dimension;
        this.options = options;
        this.fewerInfoOptionIndex = fewerInfoOptionIndex;
    }

    public void setOptions(List<String> options) {
        requireValidOptions(id, options);
        this.options = options;
    }

    private static void requireValidOptions(String id, List<String> options) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("Question " + id + " 必须恰好 4 个选项");
        }
    }

    public double optionOffset(int optionIndex) {
        return switch (optionIndex) {
            case 0 -> -0.75;
            case 1 -> -0.25;
            case 2 -> +0.25;
            case 3 -> +0.75;
            default -> throw new IllegalArgumentException("非法选项索引: " + optionIndex);
        };
    }
}