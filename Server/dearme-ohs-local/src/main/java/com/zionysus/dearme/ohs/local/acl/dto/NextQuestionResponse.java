package com.zionysus.dearme.ohs.local.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 答题推进响应。
 * nextQuestion 为 null 表示已答完，client 应调 /report 取报告。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextQuestionResponse {

    private NextQuestion nextQuestion;
    private int answeredCount;
    private boolean done;
    private Double topCandidateProbability;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextQuestion {
        private String id;
        private String prompt;
        private List<String> options;
    }
}