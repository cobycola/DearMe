package com.zionysus.dearme.acl.ports;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;

import java.util.List;
import java.util.Map;

/**
 * 报告生成端口（南向 outbound port）。
 *
 * 主实现：LlmReportGeneratorAdapter —— 调 Spring AI ChatClient.entity() 强转结构化输出。
 * 降级实现：TemplateReportGeneratorAdapter —— LLM 失败时走固定模板，保证「付费必得报告」。
 *
 * 直接同进程调用，不走 RPC（按用户要求：ai 子包放本项目内，直接接口调用）。
 */
public interface ReportGeneratorPort {

    /**
     * 生成报告。返回 Markdown 文本。
     * 实现：
     *   - 必须只用 candidates + answered 维度信息，不编未答维度
     *   - 必须校验返回的候选名在白名单内
     *   - LLM 失败必须降级模板，不能返回 null 或抛异常给用户
     */
    String generate(InferenceSummary summary,
                    List<CharacterProfile> candidates,
                    List<Answer> answered,
                    Map<String, Question> questionById);
}