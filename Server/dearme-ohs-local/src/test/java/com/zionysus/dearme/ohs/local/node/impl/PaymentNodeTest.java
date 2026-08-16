package com.zionysus.dearme.ohs.local.node.impl;

import com.zionysus.dearme.acl.infra.persistence.InMemoryPaymentRepository;
import com.zionysus.dearme.acl.infra.persistence.InMemorySessionRepository;
import com.zionysus.dearme.acl.ports.PaymentGatewayPort;
import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.payment.PaymentStatus;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentNodeTest {

    private static PaymentNode node(Session session,
                                    PaymentGatewayPort gateway,
                                    InMemorySessionRepository sessions,
                                    InMemoryPaymentRepository payments) {
        sessions.save(session);
        return new PaymentNode(sessions, gateway, payments);
    }

    private static SessionFlowContext charge(PaymentNode node, String sessionId) {
        SessionFlowContext ctx = new SessionFlowContext();
        ctx.setSessionId(sessionId);
        ctx.setAmountCents(1000);
        node.execute(ctx);
        return ctx;
    }

    @Test
    void shouldUnlockSessionOnSuccessfulCharge() {
        InMemorySessionRepository sessions = new InMemorySessionRepository();
        InMemoryPaymentRepository payments = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markSuccess("txn-1"); return p; };

        Session s = new Session("anime-character");
        PaymentNode node = node(s, gateway, sessions, payments);

        SessionFlowContext ctx = charge(node, s.getId());

        assertThat(ctx.isSessionUnlocked()).isTrue();
        assertThat(ctx.getSession().getStatus().name()).isEqualTo("PAID");
        assertThat(ctx.getPayment().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldKeepSessionUnpaidWhenChargeFails() {
        InMemorySessionRepository sessions = new InMemorySessionRepository();
        InMemoryPaymentRepository payments = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markFailed("insufficient"); return p; };

        Session s = new Session("anime-character");
        PaymentNode node = node(s, gateway, sessions, payments);

        SessionFlowContext ctx = charge(node, s.getId());

        assertThat(ctx.isSessionUnlocked()).isFalse();
        assertThat(ctx.getSession().getStatus().name()).isEqualTo("CREATED");
        assertThat(ctx.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldBeIdempotentWhenSessionAlreadyPaid() {
        InMemorySessionRepository sessions = new InMemorySessionRepository();
        InMemoryPaymentRepository payments = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markSuccess("txn-1"); return p; };

        Session s = new Session("anime-character");
        PaymentNode node = node(s, gateway, sessions, payments);

        Payment first = charge(node, s.getId()).getPayment();
        assertThat(payments.findById(first.getId())).isPresent();

        // 第二次付费：命中幂等直接返回成功，不再向 gateway 发起新请求
        SessionFlowContext ctx2 = charge(node, s.getId());

        assertThat(ctx2.isIdempotentHit()).isTrue();
        assertThat(ctx2.getPayment().getId()).isEqualTo(first.getId());
        assertThat(ctx2.getPayment().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldKeepPaymentRecordWhenTimeout() {
        InMemorySessionRepository sessions = new InMemorySessionRepository();
        InMemoryPaymentRepository payments = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markTimeout(); return p; };

        Session s = new Session("anime-character");
        PaymentNode node = node(s, gateway, sessions, payments);

        SessionFlowContext ctx = charge(node, s.getId());

        assertThat(ctx.isSessionUnlocked()).isFalse();
        assertThat(ctx.getPayment().getStatus()).isEqualTo(PaymentStatus.TIMEOUT);
        assertThat(payments.findById(ctx.getPayment().getId())).isPresent();
    }
}
