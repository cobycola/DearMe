package com.zionysus.dearme.acl.infra.persistence.jdbc;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Session 仓储 JDBC 实现（南向，PostgreSQL + jsonb 整列存聚合根）。
 * 仅在 dearme.persistence=jdbc 时装配；缺省由 InMemorySessionRepository 接管。
 *
 * 反序列化链路：DB body(jsonb) → SessionRecord → Session.reconstitute。
 * domain 无需加 Jackson 注解，透 record + reconstitute 完成水合。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dearme.persistence", havingValue = "jdbc")
public class JdbcSessionRepository implements SessionRepositoryPort {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public Session save(Session session) {
        SessionRecord rec = new SessionRecord(
                session.getId(), session.getTopicId(), session.getCreatedAt(),
                session.getStatus().name(),
                session.getAnswers(),
                session.getReportContent(),
                session.getExpiredAt());
        String body = toJson(rec);
        int affected = jdbc.update(
                "INSERT INTO sessions (id, topic_id, status, created_at, body) " +
                        "VALUES (?, ?, ?, ?, ?::jsonb) " +
                        "ON CONFLICT (id) DO UPDATE SET topic_id=EXCLUDED.topic_id, " +
                        "status=EXCLUDED.status, created_at=EXCLUDED.created_at, body=EXCLUDED.body",
                session.getId(), session.getTopicId(), session.getStatus().name(),
                Timestamp.from(session.getCreatedAt()), body);
        if (affected == 0) {
            throw new IllegalStateException("Session save 未写入任何行: " + session.getId());
        }
        return session;
    }

    @Override
    public Optional<Session> findById(String id) {
        return jdbc.query(
                        "SELECT body FROM sessions WHERE id = ?",
                        (rs, i) -> rs.getString("body"),
                        id)
                .stream()
                .findFirst()
                .map(this::toSession);
    }

    private String toJson(SessionRecord rec) {
        try {
            return objectMapper.writeValueAsString(rec);
        } catch (Exception e) {
            throw new IllegalStateException("Session 序列化失败: " + rec.getId(), e);
        }
    }

    private Session toSession(String body) {
        try {
            SessionRecord rec = objectMapper.readValue(body, SessionRecord.class);
            return Session.reconstitute(
                    rec.getId(), rec.getTopicId(), rec.getCreatedAt(), rec.statusEnum(),
                    rec.getAnswers(), rec.getReportContent(), rec.getExpiredAt());
        } catch (Exception e) {
            throw new IllegalStateException("Session 反序列化失败", e);
        }
    }
}