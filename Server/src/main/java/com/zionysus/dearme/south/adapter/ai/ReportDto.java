package com.zionysus.dearme.south.adapter.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 报告生成的结构化输出 DTO。
 *
 * 通过 Spring AiWorker.entity(ReportDto.class) 强转。
 * 字段设计为 LLM 易理解的挂载点：
 *   - matchedCharacterId：必须与候选库某 id 完全一致（白名单校验）
 *   - sections：报告章节，自由文本（受 prompt 约束不编造未答维度）
 *
 * 不直接作 HTTP 响应 DTO，ReportAppService 会再转体给 north acl dto。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    @JsonProperty("matchedCharacterId")
    private String matchedCharacterId;
    @JsonProperty("matchedCharacterName")
    private String matchedCharacterName;
    @JsonProperty("headline")
    private String headline;
    @JsonProperty("analysis")
    private String analysis;
    @JsonProperty("advice")
    private String advice;
}