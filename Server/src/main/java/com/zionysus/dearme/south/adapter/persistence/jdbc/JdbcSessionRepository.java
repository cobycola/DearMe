package com.zionysus.dearme.south.adapter.persistence.jdbc;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.south.port.SessionRepositoryPort;
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
    public Session save(Session s) {
        SessionRecord rec = new SessionRecord(
                s.getId(), s.getTopicId(), s.getCreatedAt(),
                s.getStatus().name(),
                s.getAnswers(),
                s.getReportContent(),
                s.getExpiredAt());
        String body = toJson(rec);
        int affected = jdbc.update(
                "INSERT INTO sessions (id, topic_id, status, created_at, body) " +
                        "VALUES (?, ?, ?, ?, ?::jsonb) " +
                        "ON CONFLICT (id) DO UPDATE SET topic_id=EXCLUDED.topic_id, " +
                        "status=EXCLUDED.status, created_at=EXCLUDED.created_at, body=EXCLUDED.body",
                s.getId(), s.getTopicId(), s.getStatus().name(),
                Timestamp.from(s.getCreatedAt()), body);
        if (affected == 0) {
            throw new IllegalStateException("Session save 未写入任何行: " + s.getId());
        }
        return s;
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