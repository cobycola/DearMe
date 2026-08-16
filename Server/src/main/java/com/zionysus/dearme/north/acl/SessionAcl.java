package com.zionysus.dearme.north.acl;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.node.InferenceNode;
import com.zionysus.dearme.north.acl.dto.AnswerResultView;
import com.zionysus.dearme.north.acl.dto.CreateSessionRequest;
import com.zionysus.dearme.north.acl.dto.CreateSessionResponse;
import com.zionysus.dearme.north.acl.dto.NextQuestionResponse;
import org.springframework.stereotype.Component;

/**
 * Session 防腐层。把 DTO 转换为 AppService 入参，把领域返回组装成响应 DTO。
 *
 * 未来接 gRPC/MQ 入站时，新的 ACL 实现此相同职责即可复用 AppService。
 */
@Component
public class SessionAcl {

    public String topicIdOf(CreateSessionRequest request) {
        return request.getTopicId();
    }

    public CreateSessionResponse toCreate(Session session) {
        return new CreateSessionResponse(session.getId(), session.getStatus().name(), session.getTopicId());
    }

    /**
     * 把 InferenceNode.Result 转 client-facing 响应。
     * 抹掉领域内部 InferenceSummary 全量信息，只暴露题面 + 进度 + 主候选概率。
     */
    public AnswerResultView toAnswerResult(Session session,
                                            InferenceNode.Result result,
                                            boolean isFirst) {
        if (result == null) {
            // InferenceNode 出错（非法状态或 LLM 失败级联），仅展示进度不带题
            return new AnswerResultView(
                    new NextQuestionResponse(
                            null,
                            session == null ? 0 : session.getAnswers().size(),
                            true,
                            null
                    ),
                    isFirst
            );
        }
        NextQuestionResponse.NextQuestion nextQuestion = result.getNextQuestion() == null ? null
                : new NextQuestionResponse.NextQuestion(
                        result.getNextQuestion().getId(),
                        result.getNextQuestion().getPrompt(),
                        result.getNextQuestion().getOptions()
                );
        return new AnswerResultView(
                new NextQuestionResponse(
                        nextQuestion,
                        session.getAnswers().size(),
                        nextQuestion == null,
                        result.getSummary() == null || result.getSummary().getTopCandidates().isEmpty()
                                ? null
                                : result.getSummary().getTopCandidates().get(0).getValue()
                ),
                isFirst
        );
    }
}
