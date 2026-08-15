package com.zionysus.dearme.domain.question;

/**
 * 题型。MVP 只做单选量表题：四选一，每选项对应一个特征维度的强度偏移。
 * 后续可扩展 LIKERT、OPEN（开放简答，走 LLM）。
 */
public enum QuestionType {
    SINGLE_CHOICE_SCALE
}