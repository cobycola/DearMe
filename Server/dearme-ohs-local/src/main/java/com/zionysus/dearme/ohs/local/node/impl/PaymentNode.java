package com.zionysus.dearme.ohs.local.node.impl;

import com.zionysus.dearme.acl.ports.PaymentGatewayPort;
import com.zionysus.dearme.acl.ports.PaymentRepositoryPort;
import com.zionysus.dearme.acl.ports.SessionRepositoryPort;
import com.zionysus.dearme.common.model.Result;
import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.session.SessionTransition;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import com.zionysus.dearme.ohs.local.node.AbstractSessionNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付节点（用例：POST /api/payments）。
 *
 * before：幂等查（同 session 已成功支付直接复用）+ assertCanPay 守卫；
 * deal：建 Payment 并调网关；after：落仓储，成功则 markPaid + 存 session。
 */
@Slf4j
@Component
public class PaymentNode extends AbstractSessionNodeService {

    private final PaymentGatewayPort paymentGateway;
    private final PaymentRepositoryPort paymentRepository;

    public PaymentNode(SessionRepositoryPort sessionRepository,
                       PaymentGatewayPort paymentGateway,
                       PaymentRepositoryPort paymentRepository) {
        super(sessionRepository);
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
    }

    @Override
    protected boolean before(SessionFlowContext context) {
        if (loadSession(context) == null) {
            return false;
        }
        // D5 幂等：同 session 已有成功支付，直接返回该记录，不再向网关发起
        Payment existing = paymentRepository.findSuccessBySessionId(context.getSession().getId()).orElse(null);
        if (existing != null) {
            context.setPayment(existing);
            context.setIdempotentHit(true);
            context.setSessionUnlocked(true);
            return false;
        }
        try {
            SessionTransition.assertCanPay(context.getSession());
        } catch (IllegalStateException e) {
            log.error("[PaymentNode] 当前状态不允许支付: {}", context.getSession().getStatus());
            context.setResult(Result.fail("STATE_CONFLICT", "当前状态不允许支付"));
            return false;
        }
        return true;
    }

    @Override
    protected void deal(SessionFlowContext context) {
        Payment payment = new Payment(context.getSession().getId(), context.getAmountCents());
        paymentGateway.charge(payment);
        context.setPayment(payment);
    }

    @Override
    protected void after(SessionFlowContext context) {
        paymentRepository.save(context.getPayment());
        if (context.getPayment().isCompleted()) {
            SessionTransition.markPaid(context.getSession());
        }
        context.setIdempotentHit(false);
        context.setSessionUnlocked(context.getPayment().isCompleted());
        sessionRepository.save(context.getSession());
    }
}
