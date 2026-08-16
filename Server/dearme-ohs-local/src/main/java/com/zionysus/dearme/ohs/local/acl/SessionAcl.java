package com.zionysus.dearme.ohs.local.acl;

import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.ohs.local.acl.dto.AnswerResultView;
import com.zionysus.dearme.ohs.local.acl.dto.CreateSessionRequest;
import com.zionysus.dearme.ohs.local.acl.dto.CreateSessionResponse;
import com.zionysus.dearme.ohs.local.acl.dto.NextQuestionResponse;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import org.springframework.stereotype.Component;

/**
 * Session 防腐层（北向）。DTO ↔ 流程上下文 / 领域返回 的转换。
 */
@Component
public class SessionAcl {

    public String topicIdOf(CreateSessionRequest request) {
        return request.getTopicId();
    }

    public CreateSessionResponse toCreate(SessionFlowContext context) {
        Session session = context.getSession();
        return new CreateSessionResponse(session.getId(), session.getStatus().name(), session.getTopicId());
    }

    /**
     * 把流程上下文转 client-facing 响应。
     * 抹掉领域内部 InferenceSummary 全量信息，只暴露题面 + 进度 + 主候选概率。
     */
    public AnswerResultView toAnswerResult(SessionFlowContext context, boolean isFirst) {
        Question nextQuestion = context.getNextQuestion();
        NextQuestionResponse.NextQuestion nq = nextQuestion == null ? null
                : new NextQuestionResponse.NextQuestion(
                        nextQuestion.getId(),
                        nextQuestion.getPrompt(),
                        nextQuestion.getOptions());
        InferenceSummary summary = context.getSummary();
        Double topProb = (summary == null || summary.getTopCandidates().isEmpty())
                ? null
                : summary.getTopCandidates().get(0).getValue();
        Session session = context.getSession();
        int answeredCount = session == null ? 0 : session.getAnswers().size();
        boolean done = nq == null;
        return new AnswerResultView(new NextQuestionResponse(nq, answeredCount, done, topProb), isFirst);
    }
}
