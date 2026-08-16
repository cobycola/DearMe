package com.zionysus.dearme.application;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.node.PaymentVerifyNode;
import com.zionysus.dearme.south.port.SessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付应用服务（编排）。调 PaymentVerifyNode 完成幂等校验、网关调用、状态推进。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAppService {

    private final PaymentVerifyNode paymentVerifyNode;
    private final SessionRepositoryPort sessionRepository;

    /** 发起支付。成功解锁 session 为 PAID；session 不存在返 null，由 Controller 转 4xx。 */
    public PaymentVerifyNode.PaymentResult pay(String sessionId, long amountCents) {
        Session session = sessionRepository.findById(sessionId).orElseGet(() -> {
            log.error("[PaymentAppService] session 不存在: {}", sessionId);
            return null;
        });
        if (session == null) {
            return null;
        }
        PaymentVerifyNode.PaymentResult result = paymentVerifyNode.charge(session, amountCents);
        sessionRepository.save(session);
        return result;
    }
}