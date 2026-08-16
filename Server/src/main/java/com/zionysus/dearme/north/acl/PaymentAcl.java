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

    public PaymentRequestCmd toCmd(PaymentRequest request) {
        return new PaymentRequestCmd(request.getSessionId(), request.getAmountCents());
    }

    public PaymentResponse toResponse(PaymentVerifyNode.PaymentResult result) {
        if (result == null) {
            return null;
        }
        Payment payment = result.getPayment();
        return new PaymentResponse(
                payment.getId(),
                payment.getSessionId(),
                payment.getStatus().name(),
                result.isSessionUnlocked(),
                result.isIdempotentHit()
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