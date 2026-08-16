package com.zionysus.dearme.acl.infra.persistence.jdbc;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.acl.ports.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Payment 仓储 JDBC 实现（南向，PostgreSQL + jsonb 整列存聚合根）。
 * 仅在 dearme.persistence=jdbc 时装配；缺省由 InMemoryPaymentRepository 接管。
 *
 * 幂等兜底：payments 表有部分唯一索引 ux_payments_success_per_session（同 session 至多一条 SUCCESS）。
 * save 并发两笔 SUCCESS 时，second 落唯一索引抛 DuplicateKeyException，吞掉回查既有成功单返回。
 * 业务幂等入口仍在 PaymentVerifyNode#charge 的 findSuccessBySessionId 先查路径，DB 索引只是 TOCTOU 兜底。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dearme.persistence", havingValue = "jdbc")
public class JdbcPaymentRepository implements PaymentRepositoryPort {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public Payment save(Payment p) {
        PaymentRecord rec = new PaymentRecord(
                p.getId(), p.getSessionId(), p.getAmountCents(), p.getCreatedAt(),
                p.getStatus().name(), p.getExternalTxnId(), p.getFailureReason());
        String body = toJson(rec);
        try {
            int affected = jdbc.update(
                    "INSERT INTO payments (id, session_id, status, created_at, body) " +
                            "VALUES (?, ?, ?, ?, ?::jsonb) " +
                            "ON CONFLICT (id) DO UPDATE SET session_id=EXCLUDED.session_id, " +
                            "status=EXCLUDED.status, created_at=EXCLUDED.created_at, body=EXCLUDED.body",
                    p.getId(), p.getSessionId(), p.getStatus().name(),
                    Timestamp.from(p.getCreatedAt()), body);
            if (affected == 0) {
                throw new IllegalStateException("Payment save 未写入任何行: " + p.getId());
            }
        } catch (DuplicateKeyException e) {
            // 部分唯一索引 ux_payments_success_per_session 拒绝并发两笔 SUCCESS：回查既有成功单幂等返回。
            log.info("Payment 并发幂等命中唯一索引，回查 sessionId={}", p.getSessionId());
            return findSuccessBySessionId(p.getSessionId())
                    .orElseThrow(() -> new IllegalStateException(
                            "DuplicateKey 但回查未找到 SUCCESS payment，sessionId=" + p.getSessionId(), e));
        }
        return p;
    }

    @Override
    public Optional<Payment> findById(String id) {
        return jdbc.query(
                        "SELECT body FROM payments WHERE id = ?",
                        (rs, i) -> rs.getString("body"),
                        id)
                .stream()
                .findFirst()
                .map(this::toPayment);
    }

    @Override
    public Optional<Payment> findSuccessBySessionId(String sessionId) {
        return jdbc.query(
                        "SELECT body FROM payments WHERE session_id = ? AND status = 'SUCCESS' LIMIT 1",
                        (rs, i) -> rs.getString("body"),
                        sessionId)
                .stream()
                .findFirst()
                .map(this::toPayment);
    }

    private String toJson(PaymentRecord rec) {
        try {
            return objectMapper.writeValueAsString(rec);
        } catch (Exception e) {
            throw new IllegalStateException("Payment 序列化失败: " + rec.getId(), e);
        }
    }

    private Payment toPayment(String body) {
        try {
            PaymentRecord rec = objectMapper.readValue(body, PaymentRecord.class);
            return Payment.reconstitute(
                    rec.getId(), rec.getSessionId(), rec.getAmountCents(), rec.getCreatedAt(),
                    rec.statusEnum(), rec.getExternalTxnId(), rec.getFailureReason());
        } catch (Exception e) {
            throw new IllegalStateException("Payment 反序列化失败", e);
        }
    }
}