package com.zionysus.dearme.ohs.local.node.impl;

import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import com.zionysus.dearme.acl.ports.TopicRegistryPort;
import com.zionysus.dearme.common.model.Result;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import com.zionysus.dearme.ohs.local.node.AbstractNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 创建 Session 节点（用例：POST /api/sessions）。
 * before：校验主题存在；deal：建聚合根；after：落仓储。
 */
@Slf4j
@Component
public class CreateSessionNode extends AbstractNodeService<SessionFlowContext> {

    private final TopicRegistryPort topicRegistry;
    private final SessionRepositoryPort sessionRepository;

    public CreateSessionNode(TopicRegistryPort topicRegistry, SessionRepositoryPort sessionRepository) {
        this.topicRegistry = topicRegistry;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected boolean before(SessionFlowContext context) {
        if (topicRegistry.topic(context.getTopicId()).isEmpty()) {
            log.error("[CreateSessionNode] 未知主题: {}", context.getTopicId());
            context.setResult(Result.fail("UNKNOWN_TOPIC", "未知主题"));
            return false;
        }
        return true;
    }

    @Override
    protected void deal(SessionFlowContext context) {
        context.setSession(new Session(context.getTopicId()));
    }

    @Override
    protected void after(SessionFlowContext context) {
        sessionRepository.save(context.getSession());
    }
}
