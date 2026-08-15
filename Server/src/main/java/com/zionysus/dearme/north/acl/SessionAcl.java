package com.zionysus.dearme.north.acl;

import com.zionysus.dearme.domain.session.Session;
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

    public String topicIdOf(CreateSessionRequest req) {
        return req.getTopicId();
    }

    public CreateSessionResponse toCreate(Session s) {
        return new CreateSessionResponse(s.getId(), s.getStatus().name(), s.getTopicId());
    }

    /**
     * 把 InferenceNode.Result 转 client-facing 响应。
     * 抹掉领域内部 InferenceSummary 全量信息，只暴露题面 + 进度 + 主候选概率。
     */
    public AnswerResultView toAnswerResult(Session s,
                                            com.zionysus.dearme.node.InferenceNode.Result r,
                                            boolean isFirst) {
        if (r == null) {
            // InferenceNode 出错（非法状态或 LLM 失败级联），仅展示进度不带题
            return new AnswerResultView(
                    new NextQuestionResponse(
                            null,
                            s == null ? 0 : s.getAnswers().size(),
                            true,
                            null
                    ),
                    isFirst
            );
        }
        var nq = r.getNextQuestion() == null ? null : new NextQuestionResponse.NextQuestion(
                r.getNextQuestion().getId(),
                r.getNextQuestion().getPrompt(),
                r.getNextQuestion().getOptions()
        );
        return new AnswerResultView(
                new NextQuestionResponse(
                        nq,
                        s.getAnswers().size(),
                        nq == null,
                        r.getSummary() == null || r.getSummary().getTopCandidates().isEmpty()
                                ? null
                                : r.getSummary().getTopCandidates().get(0).getValue()
                ),
                isFirst
        );
    }
}