package com.zionysus.dearme.application;

import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionStatus;
import com.zionysus.dearme.node.InferenceNode;
import com.zionysus.dearme.south.port.SessionRepositoryPort;
import com.zionysus.dearme.south.port.TopicRegistryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Session 应用服务（编排，不写业务）。
 *
 * 用例流：
 *   POST /sessions              → 创建 session（CREATED）
 *   POST /payments              → 付费成功后 PAID
 *   POST /sessions/{id}/first-question → 推出首题（session 转 ASKING）
 *   POST /sessions/{id}/answers  → 提交答案 + 推出下题（题数为 null 时进入 ANSWERED_ALL）
 *   POST /sessions/{id}/report   → 生成报告（REPORT_READY）
 *
 * 业务非法不抛异常，记 ERROR 后返回 null，Controller 层据此返 4xx ApiError。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionAppService {

    private final SessionRepositoryPort sessionRepository;
    private final TopicRegistryPort topicRegistry;
    private final InferenceNode inferenceNode;

    public Session create(String topicId) {
        if (topicRegistry.topic(topicId).isEmpty()) {
            log.error("[SessionAppService] 未知主题: {}", topicId);
            return null;
        }
        Session s = new Session(topicId);
        return sessionRepository.save(s);
    }

    public Session require(String sessionId) {
        return sessionRepository.findById(sessionId).orElseGet(() -> {
            log.error("[SessionAppService] session 不存在: {}", sessionId);
            return null;
        });
    }

    /** 在 PAID 状态下调，推出首题，session 转 ASKING。 */
    public InferenceNode.Result firstQuestion(String sessionId) {
        Session s = require(sessionId);
        if (s == null) {
            return null;
        }
        if (s.getStatus() != SessionStatus.PAID) {
            log.error("[SessionAppService] 仅 PAID 状态可请求首题，当前: {}", s.getStatus());
            return null;
        }
        InferenceNode.Result r = inferenceNode.firstQuestion(s);
        sessionRepository.save(s);
        return r;
    }

    /** 提交答案并推出下题。题已答完返回 nextQuestion=null。 */
    public InferenceNode.Result submitAnswer(String sessionId, String questionId, int optionIndex) {
        Session s = require(sessionId);
        if (s == null) {
            return null;
        }
        if (s.getStatus() != SessionStatus.ASKING) {
            log.error("[SessionAppService] 仅 ASKING 状态可提交答案，当前: {}", s.getStatus());
            return null;
        }
        Answer answer = new Answer(questionId, optionIndex);
        InferenceNode.Result r = inferenceNode.nextQuestion(s, answer);
        sessionRepository.save(s);
        return r;
    }
}