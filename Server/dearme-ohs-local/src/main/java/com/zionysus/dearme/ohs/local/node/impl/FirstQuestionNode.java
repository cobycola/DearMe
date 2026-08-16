package com.zionysus.dearme.ohs.local.node.impl;

import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import com.zionysus.dearme.acl.ports.TopicRegistryPort;
import com.zionysus.dearme.common.model.Result;
import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.engine.InferenceEngine;
import com.zionysus.dearme.domain.inference.policy.NextQuestionPolicy;
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
 * 首题节点（用例：POST /api/sessions/{id}/first-question）。
 * before：assertCanAnswer + markAsking；deal：推理 + 选首题；after：落仓储。
 */
@Slf4j
@Component
public class FirstQuestionNode extends AbstractSessionNodeService {

    private final TopicRegistryPort topicRegistry;
    private final InferenceEngine inferenceEngine;
    private final NextQuestionPolicy nextQuestionPolicy;

    public FirstQuestionNode(SessionRepositoryPort sessionRepository,
                             TopicRegistryPort topicRegistry,
                             InferenceEngine inferenceEngine,
                             NextQuestionPolicy nextQuestionPolicy) {
        super(sessionRepository);
        this.topicRegistry = topicRegistry;
        this.inferenceEngine = inferenceEngine;
        this.nextQuestionPolicy = nextQuestionPolicy;
    }

    @Override
    protected boolean before(SessionFlowContext context) {
        if (loadSession(context) == null) {
            return false;
        }
        try {
            SessionTransition.assertCanAnswer(context.getSession());
            SessionTransition.markAsking(context.getSession());
        } catch (IllegalStateException e) {
            log.error("[FirstQuestionNode] 首题非法状态: {}", e.getMessage());
            context.setResult(Result.fail("STATE_CONFLICT", e.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    protected void deal(SessionFlowContext context) {
        inferAndSelect(context);
    }

    @Override
    protected void after(SessionFlowContext context) {
        sessionRepository.save(context.getSession());
    }

    private void inferAndSelect(SessionFlowContext context) {
        Session session = context.getSession();
        List<CharacterProfile> candidates = topicRegistry.characters(session.getTopicId());
        List<Question> allQuestions = topicRegistry.questions(session.getTopicId());
        Map<String, Question> qbyId = topicRegistry.questionIndex(session.getTopicId());
        InferenceSummary summary = inferenceEngine.infer(session.getAnswers(), candidates, qbyId);
        context.setSummary(summary);
        context.setNextQuestion(nextQuestionPolicy.select(allQuestions, session.getAnswers(), candidates, qbyId));
    }
}
