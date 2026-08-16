package com.zionysus.dearme.ohs.local.appservice;

import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import com.zionysus.dearme.ohs.local.node.impl.BuildReportNode;
import com.zionysus.dearme.ohs.local.node.impl.CreateSessionNode;
import com.zionysus.dearme.ohs.local.node.impl.FirstQuestionNode;
import com.zionysus.dearme.ohs.local.node.impl.PaymentNode;
import com.zionysus.dearme.ohs.local.node.impl.SubmitAnswerNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * quiz 主链路应用服务（对应 pay-settle-front ohs-local 的 AppService）。
 *
 * 只负责「构造流程上下文 → 串链对应 Node 管道 → 返回上下文」，不写业务。
 * 5 个方法 1:1 对应 5 个 HTTP 用例；失败与否看 context.result（业务非法不抛异常）。
 */
@Service
@RequiredArgsConstructor
public class QuizAppService {

    private final CreateSessionNode createSessionNode;
    private final PaymentNode paymentNode;
    private final FirstQuestionNode firstQuestionNode;
    private final SubmitAnswerNode submitAnswerNode;
    private final BuildReportNode buildReportNode;
    private final SessionRepositoryPort sessionRepository;

    public SessionFlowContext create(String topicId) {
        SessionFlowContext context = new SessionFlowContext();
        context.setTopicId(topicId);
        createSessionNode.execute(context);
        return context;
    }

    public SessionFlowContext pay(String sessionId, long amountCents) {
        SessionFlowContext context = new SessionFlowContext();
        context.setSessionId(sessionId);
        context.setAmountCents(amountCents);
        paymentNode.execute(context);
        return context;
    }

    public SessionFlowContext firstQuestion(String sessionId) {
        SessionFlowContext context = new SessionFlowContext();
        context.setSessionId(sessionId);
        firstQuestionNode.execute(context);
        return context;
    }

    public SessionFlowContext submitAnswer(String sessionId, String questionId, int optionIndex) {
        SessionFlowContext context = new SessionFlowContext();
        context.setSessionId(sessionId);
        context.setQuestionId(questionId);
        context.setOptionIndex(optionIndex);
        submitAnswerNode.execute(context);
        return context;
    }

    public SessionFlowContext report(String sessionId) {
        SessionFlowContext context = new SessionFlowContext();
        context.setSessionId(sessionId);
        buildReportNode.execute(context);
        return context;
    }

    /** 仅读取进度（GET /api/sessions/{id}）：加载 session，不存在返回 null。 */
    public Session require(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }
}
