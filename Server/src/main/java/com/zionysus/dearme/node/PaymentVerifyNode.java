package com.zionysus.dearme.node;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionTransition;
import com.zionysus.dearme.south.port.PaymentGatewayPort;
import com.zionysus.dearme.south.port.PaymentRepositoryPort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付验证节点。被 PaymentAppService 编排：
 *   - 幂等校验：同 session 已成功支付则直接返回成功
 *   - 否则新建 Payment、调网关、持久化、回调 session.markPaid
 *
 * 单一职责，不耦合答题逻辑。
 * SessionTransition 守卫抛出在此转 log + return null。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentVerifyNode {

    private final PaymentGatewayPort paymentGateway;
    private final PaymentRepositoryPort paymentRepository;

    /**
     * 发起或复用一次支付。
     * - 若 session 已有成功支付，返回该记录（幂等）
     * - 否则新建一笔、调网关、save、若成功则 markPaid
     */
    public PaymentResult charge(Session session, long amountCents) {
        // 先查幂等：同 session 已成功支付，直接返回（不再 assertCanPay —— session 已 PAID）
        Payment existing = paymentRepository.findSuccessBySessionId(session.getId()).orElse(null);
        if (existing != null) {
            return new PaymentResult(existing, true, true);  // 幂等命中
        }

        try {
            SessionTransition.assertCanPay(session);
        } catch (IllegalStateException e) {
            log.error("[PaymentVerifyNode] 当前状态不允许支付: {}", session.getStatus());
            return null;
        }

        Payment payment = new Payment(session.getId(), amountCents);
        paymentGateway.charge(payment);
        paymentRepository.save(payment);

        if (payment.isCompleted()) {
            SessionTransition.markPaid(session);
        }
        return new PaymentResult(payment, false, payment.isCompleted());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResult {
        private Payment payment;
        private boolean idempotentHit;
        private boolean sessionUnlocked;
    }
}