package com.zionysus.dearme.node;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.engine.InferenceEngine;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionTransition;
import com.zionysus.dearme.south.port.ReportGeneratorPort;
import com.zionysus.dearme.south.port.TopicRegistryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 报告构建节点。单一职责：
 *   - 取主题数据 + 推理 summary
 *   - 调 ReportGeneratorPort 生成 Markdown 文本
 *   - 回写到 session（REPORT_READY 状态 + reportContent）
 *
 * 不做候选白名单、降级等判断——这些职责在 ReportGeneratorAdapter 内部完成。
 * SessionTransition 守卫抛出在此转 log + return null。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportBuildNode {

    private final TopicRegistryPort topicRegistry;
    private final InferenceEngine inferenceEngine;
    private final ReportGeneratorPort reportGenerator;

    public String build(Session session) {
        try {
            SessionTransition.assertCanRequestReport(session);

            List<CharacterProfile> candidates = topicRegistry.characters(session.getTopicId());
            Map<String, Question> qbyId = topicRegistry.questionIndex(session.getTopicId());
            InferenceSummary summary = inferenceEngine.infer(session.getAnswers(), candidates, qbyId);
            String markdown = reportGenerator.generate(summary, candidates, session.getAnswers(), qbyId);

            session.setReportContent(markdown);
            SessionTransition.markReportReady(session);
            return markdown;
        } catch (IllegalStateException e) {
            log.error("[ReportBuildNode] 当前状态不允许请求报告: {}", session.getStatus());
            return null;
        }
    }
}