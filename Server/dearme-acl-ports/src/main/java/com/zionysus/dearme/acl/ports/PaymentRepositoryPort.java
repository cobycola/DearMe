package com.zionysus.dearme.acl.ports;

import com.zionysus.dearme.domain.payment.Payment;

import java.util.Optional;

/**
 * Payment 仓储端口（南向 outbound port）。
 *
 * MVP 内存实现。扩展点：DB 持久化 / 对账系统集成。
 */
public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(String id);

    Optional<Payment> findSuccessBySessionId(String sessionId);
}