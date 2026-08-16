package com.zionysus.dearme.ohs.local.acl;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.ohs.local.acl.dto.PaymentRequest;
import com.zionysus.dearme.ohs.local.acl.dto.PaymentResponse;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Payment 防腐层（北向）。DTO ↔ 流程上下文 的转换。
 */
@Component
public class PaymentAcl {

    public PaymentRequestCmd toCmd(PaymentRequest request) {
        return new PaymentRequestCmd(request.getSessionId(), request.getAmountCents());
    }

    public PaymentResponse toResponse(SessionFlowContext context) {
        Payment payment = context.getPayment();
        return new PaymentResponse(
                payment.getId(),
                payment.getSessionId(),
                payment.getStatus().name(),
                context.isSessionUnlocked(),
                context.isIdempotentHit()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequestCmd {
        private String sessionId;
        private long amountCents;
    }
}
