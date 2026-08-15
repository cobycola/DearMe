package com.zionysus.dearme.south.adapter.mock;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.south.port.PaymentGatewayPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock 支付 adapter（南向）。
 *
 * 模拟三种结算：SUCCESS / FAILED / TIMEOUT。
 * 通过 successRate 配置成功率（0.0 ~ 1.0）。
 * 默认 1.0（全成功），便于冒烟；测试时可注入 0.0 / 0.5 验证降级路径。
 *
 * 接真实网关扩展点：踩「公开发布到生产」红线，用户亲自接入。预留接口的 PaymentGatewayPort
 * 即扩展点契约。
 */
@Slf4j
@Component
public class MockPaymentAdapter implements PaymentGatewayPort {

    private final double successRate;

    public MockPaymentAdapter(@Value("${dearme.payment.mock.success-rate:1.0}") double successRate) {
        Validate.inclusiveBetween(0.0, 1.0, successRate, "success-rate 必须在 [0,1]");
        this.successRate = successRate;
    }

    @Override
    public Payment charge(Payment payment) {
        log.info("Mock 支付 sessionId={}, amountCents={}", payment.getSessionId(), payment.getAmountCents());
        // 模拟超时：successRate 极低时偶尔走超时分支，否则走成功/失败二分
        double r = Math.random();
        if (r < successRate) {
            payment.markSuccess("mock-txn-" + UUID.randomUUID());
        } else if (r < successRate + 0.1) {
            payment.markTimeout();
        } else {
            payment.markFailed("mock-failure");
        }
        return payment;
    }
}