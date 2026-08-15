package com.zionysus.dearme.node;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.payment.PaymentStatus;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.south.adapter.persistence.InMemoryPaymentRepository;
import com.zionysus.dearme.south.port.PaymentGatewayPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentVerifyNodeTest {

    @Test
    void shouldUnlockSessionOnSuccessfulCharge() {
        InMemoryPaymentRepository repo = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markSuccess("txn-1"); return p; };
        PaymentVerifyNode node = new PaymentVerifyNode(gateway, repo);

        Session s = new Session("anime-character");
        PaymentVerifyNode.PaymentResult r = node.charge(s, 1000);

        assertThat(r.isSessionUnlocked()).isTrue();
        assertThat(s.getStatus().name()).isEqualTo("PAID");
        assertThat(r.getPayment().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldKeepSessionUnpaidWhenChargeFails() {
        InMemoryPaymentRepository repo = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markFailed("insufficient"); return p; };
        PaymentVerifyNode node = new PaymentVerifyNode(gateway, repo);

        Session s = new Session("anime-character");
        PaymentVerifyNode.PaymentResult r = node.charge(s, 1000);

        assertThat(r.isSessionUnlocked()).isFalse();
        assertThat(s.getStatus().name()).isEqualTo("CREATED");
        assertThat(r.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldBeIdempotentWhenSessionAlreadyPaid() {
        InMemoryPaymentRepository repo = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markSuccess("txn-1"); return p; };
        PaymentVerifyNode node = new PaymentVerifyNode(gateway, repo);

        Session s = new Session("anime-character");
        Payment first = node.charge(s, 1000).getPayment();
        assertThat(repo.findById(first.getId())).isPresent();

        // 第二次付费：命中幂等直接返回成功，不再向 gateway 发起新请求
        PaymentVerifyNode.PaymentResult r2 = node.charge(s, 1000);

        assertThat(r2.isIdempotentHit()).isTrue();
        assertThat(r2.getPayment().getId()).isEqualTo(first.getId());
        assertThat(r2.getPayment().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldKeepPaymentRecordWhenTimeout() {
        InMemoryPaymentRepository repo = new InMemoryPaymentRepository();
        PaymentGatewayPort gateway = p -> { p.markTimeout(); return p; };
        PaymentVerifyNode node = new PaymentVerifyNode(gateway, repo);

        Session s = new Session("anime-character");
        PaymentVerifyNode.PaymentResult r = node.charge(s, 1000);

        assertThat(r.isSessionUnlocked()).isFalse();
        assertThat(r.getPayment().getStatus()).isEqualTo(PaymentStatus.TIMEOUT);
        assertThat(repo.findById(r.getPayment().getId())).isPresent();
    }
}