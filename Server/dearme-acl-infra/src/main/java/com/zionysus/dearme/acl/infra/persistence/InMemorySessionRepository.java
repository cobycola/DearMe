package com.zionysus.dearme.acl.infra.persistence;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Session 内存仓储（南向，开发期/单实例 MVP）。
 *
 * 默认实现（dearme.persistence 缺省即 memory）。jdbc 模式下不装配，由 JdbcSessionRepository 接管。
 * 扩展点：换 DB 实现仍实现 SessionRepositoryPort，业务层不变。
 */
@Component
@ConditionalOnProperty(name = "dearme.persistence", havingValue = "memory", matchIfMissing = true)
public class InMemorySessionRepository implements SessionRepositoryPort {

    private final ConcurrentMap<String, Session> store = new ConcurrentHashMap<>();

    @Override
    public Session save(Session session) {
        store.put(session.getId(), session);
        return session;
    }

    @Override
    public Optional<Session> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}