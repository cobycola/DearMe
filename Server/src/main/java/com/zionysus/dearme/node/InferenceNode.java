package com.zionysus.dearme.node;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.engine.InferenceEngine;
import com.zionysus.dearme.domain.inference.policy.NextQuestionPolicy;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionStatus;
import com.zionysus.dearme.domain.session.SessionTransition;
import com.zionysus.dearme.south.port.TopicRegistryPort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 推理节点。被 AppService 编排，单一职责：
 *   - 调 TopicRegistryPort 取当前主题候选 + 题库
 *   - 调 InferenceEngine 出当前候选分布
 *   - 调 NextQuestionPolicy 出下一题（或 null 表示答完）
 *
 * 不调 persistence/状态变更/报告，仅做推理这一件事。
 * 返回值对象 {@link Result} 一次带回下题 + 推理快照，由 AppService 决定下一步。
 *
 * SessionTransition 守卫抛出在此转 log + return null，避免抛异常上抛打断控制流。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InferenceNode {

    private final TopicRegistryPort topicRegistry;
    private final InferenceEngine inferenceEngine;
    private final NextQuestionPolicy nextQuestionPolicy;

    /**
     * 当 session 首次进入 ASKING 时调：推出首题 + 推理快照。
     */
    public Result firstQuestion(Session session) {
        try {
            SessionTransition.assertCanAnswer(session);
            SessionTransition.markAsking(session);
        } catch (IllegalStateException e) {
            log.error("[InferenceNode] 首题非法状态: {}", e.getMessage());
            return null;
        }
        return inferAndSelect(session);
    }

    /**
     * 当用户提交一道答案后调：记录答案、推进推理、选下题。
     * 若下题为 null，session 标记 ANSWERED_ALL。
     */
    public Result nextQuestion(Session session, Answer answer) {
        try {
            SessionTransition.assertCanAnswer(session);
            session.addAnswer(answer);
        } catch (IllegalStateException e) {
            log.error("[InferenceNode] 提交答案非法状态: {}", e.getMessage());
            return null;
        }
        Result r = inferAndSelect(session);
        if (r.getNextQuestion() == null) {
            SessionTransition.markAllAnswered(session);
        }
        return r;
    }

    /**
     * 答完题后取最终推理快照（不下题）。
     */
    public InferenceSummary finalSummary(Session session) {
        if (session.getStatus() != SessionStatus.ANSWERED_ALL
                && session.getStatus() != SessionStatus.ASKING) {
            log.error("[InferenceNode] 当前状态不允许出报告摘要: {}", session.getStatus());
            return null;
        }
        List<CharacterProfile> candidates = topicRegistry.characters(session.getTopicId());
        Map<String, Question> qbyId = topicRegistry.questionIndex(session.getTopicId());
        return inferenceEngine.infer(session.getAnswers(), candidates, qbyId);
    }

    private Result inferAndSelect(Session session) {
        List<CharacterProfile> candidates = topicRegistry.characters(session.getTopicId());
        List<Question> allQuestions = topicRegistry.questions(session.getTopicId());
        Map<String, Question> qbyId = topicRegistry.questionIndex(session.getTopicId());

        InferenceSummary summary = inferenceEngine.infer(session.getAnswers(), candidates, qbyId);
        Question next = nextQuestionPolicy.select(allQuestions, session.getAnswers(), candidates, qbyId);
        return new Result(next, summary);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private Question nextQuestion;
        private InferenceSummary summary;
    }
}