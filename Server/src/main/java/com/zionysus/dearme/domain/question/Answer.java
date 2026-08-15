package com.zionysus.dearme.domain.question;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户对一题的回答。optionIndex 0~3。
 *
 * 业务构造走全参 {@link #Answer(String, int)}，带 0~3 校验。
 * Jackson 反序列化走无参 + setter，setter 同样带 0~3 校验，避免脏数据从 jsonb 流入业务层。
 */
@Data
@NoArgsConstructor
@Builder
public class Answer {

    private String questionId;
    private int optionIndex;

    public Answer(String questionId, int optionIndex) {
        requireValid(optionIndex);
        this.questionId = questionId;
        this.optionIndex = optionIndex;
    }

    public void setOptionIndex(int optionIndex) {
        requireValid(optionIndex);
        this.optionIndex = optionIndex;
    }

    private static void requireValid(int optionIndex) {
        if (optionIndex < 0 || optionIndex > 3) {
            throw new IllegalArgumentException("optionIndex 必须在 0~3，收到: " + optionIndex);
        }
    }
}