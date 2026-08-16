package com.zionysus.dearme.acl.ports;

import com.zionysus.dearme.domain.session.Session;

import java.util.Optional;

/**
 * Session 仓储端口（南向 outbound port）。
 *
 * MVP 内存实现。扩展点：换 DB、Redis 等持久化实现仍实现此端口。
 * 仓储而非 DAO，与 DDD 的 Aggregate Root 概念匹配。
 */
public interface SessionRepositoryPort {

    Session save(Session session);

    Optional<Session> findById(String id);
}