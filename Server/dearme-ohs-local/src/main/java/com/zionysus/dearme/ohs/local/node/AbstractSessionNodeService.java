package com.zionysus.dearme.ohs.local.node;

import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import com.zionysus.dearme.common.model.Result;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 需要按 sessionId 加载聚合根的 Node 公共基类。
 *
 * 复用 {@link #loadSession(SessionFlowContext)}，避免 4 个 session 类 Node 各写一份
 * 「findById → 不存在则记日志 + 写 context 失败」的重复代码。
 */
@Slf4j
public abstract class AbstractSessionNodeService extends AbstractNodeService<SessionFlowContext> {

    protected final SessionRepositoryPort sessionRepository;

    protected AbstractSessionNodeService(SessionRepositoryPort sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * 按 context.sessionId 加载 session 写入 context；不存在返回 null。
     * 调用方在 before() 中 `if (loadSession(context) == null) return false;`。
     */
    protected Session loadSession(SessionFlowContext context) {
        Session session = sessionRepository.findById(context.getSessionId()).orElse(null);
        if (session == null) {
            log.error("[{}] session 不存在: {}", getClass().getSimpleName(), context.getSessionId());
            context.setResult(Result.fail("NOT_FOUND", "session 不存在"));
            return null;
        }
        context.setSession(session);
        return session;
    }
}
