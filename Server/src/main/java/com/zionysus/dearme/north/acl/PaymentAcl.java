package com.zionysus.dearme.north.acl;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.north.acl.dto.PaymentRequest;
import com.zionysus.dearme.north.acl.dto.PaymentResponse;
import com.zionysus.dearme.node.PaymentVerifyNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Payment 防腐层。DTO 转换。
 */
@Component
public class PaymentAcl {

    public PaymentRequestCmd toCmd(PaymentRequest req) {
        return new PaymentRequestCmd(req.getSessionId(), req.getAmountCents());
    }

    public PaymentResponse toResponse(PaymentVerifyNode.PaymentResult r) {
        if (r == null) {
            return null;
        }
        Payment p = r.getPayment();
        return new PaymentResponse(
                p.getId(),
                p.getSessionId(),
                p.getStatus().name(),
                r.isSessionUnlocked(),
                r.isIdempotentHit()
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