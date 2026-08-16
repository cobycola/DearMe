package com.zionysus.dearme.ohs.local.node.impl;

import com.zionysus.dearme.acl.ports.ReportGeneratorPort;
import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import com.zionysus.dearme.acl.ports.TopicRegistryPort;
import com.zionysus.dearme.common.model.Result;
import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.engine.InferenceEngine;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionTransition;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import com.zionysus.dearme.ohs.local.node.AbstractSessionNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 报告构建节点（用例：POST /api/sessions/{id}/report）。
 * before：assertCanRequestReport；deal：推理 summary + 生成 Markdown + markReportReady；after：落仓储。
 */
@Slf4j
@Component
public class BuildReportNode extends AbstractSessionNodeService {

    private final TopicRegistryPort topicRegistry;
    private final InferenceEngine inferenceEngine;
    private final ReportGeneratorPort reportGenerator;

    public BuildReportNode(SessionRepositoryPort sessionRepository,
                           TopicRegistryPort topicRegistry,
                           InferenceEngine inferenceEngine,
                           ReportGeneratorPort reportGenerator) {
        super(sessionRepository);
        this.topicRegistry = topicRegistry;
        this.inferenceEngine = inferenceEngine;
        this.reportGenerator = reportGenerator;
    }

    @Override
    protected boolean before(SessionFlowContext context) {
        if (loadSession(context) == null) {
            return false;
        }
        try {
            SessionTransition.assertCanRequestReport(context.getSession());
        } catch (IllegalStateException e) {
            log.error("[BuildReportNode] 当前状态不允许请求报告: {}", context.getSession().getStatus());
            context.setResult(Result.fail("STATE_CONFLICT", e.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    protected void deal(SessionFlowContext context) {
        try {
            Session session = context.getSession();
            List<CharacterProfile> candidates = topicRegistry.characters(session.getTopicId());
            Map<String, Question> qbyId = topicRegistry.questionIndex(session.getTopicId());
            InferenceSummary summary = inferenceEngine.infer(session.getAnswers(), candidates, qbyId);
            context.setSummary(summary);

            String markdown = reportGenerator.generate(summary, candidates, session.getAnswers(), qbyId);
            context.setReportMarkdown(markdown);
            session.setReportContent(markdown);
            SessionTransition.markReportReady(session);
        } catch (IllegalStateException e) {
            log.error("[BuildReportNode] 当前状态不允许请求报告: {}", context.getSession().getStatus());
            context.setResult(Result.fail("STATE_CONFLICT", e.getMessage()));
        }
    }

    @Override
    protected void after(SessionFlowContext context) {
        if (context.isOk()) {
            sessionRepository.save(context.getSession());
        }
    }
}
