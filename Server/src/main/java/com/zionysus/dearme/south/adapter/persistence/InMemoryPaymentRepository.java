package com.zionysus.dearme.south.adapter.persistence;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.payment.PaymentStatus;
import com.zionysus.dearme.south.port.PaymentRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Payment 内存仓储（南向）。
 *
 * findSuccessBySessionId 用于幂等校验：同一 session 已有成功支付则不发起新的。
 * 默认实现（dearme.persistence 缺省即 memory）。jdbc 模式由 JdbcPaymentRepository 接管，DB 唯一索引做幂等兜底。
 */
@Component
@ConditionalOnProperty(name = "dearme.persistence", havingValue = "memory", matchIfMissing = true)
public class InMemoryPaymentRepository implements PaymentRepositoryPort {

    private final ConcurrentMap<String, Payment> byId = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        byId.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Payment> findSuccessBySessionId(String sessionId) {
        return byId.values().stream()
                .filter(p -> sessionId.equals(p.getSessionId()) && p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst();
    }
}