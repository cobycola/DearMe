package com.zionysus.dearme.domain.inference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试主题。值对象。
 *
 * 多主题扩展时，每个主题绑定各自的 CharacterSource / QuestionSource 实现，
 * 并可绑定不同 InferenceEngine。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    private String id;
    private String displayName;
    private String description;
    private String characterSourceKey;
    private String questionSourceKey;
    private String inferenceEngineKey;
}