package com.zionysus.dearme.north.web;

import com.zionysus.dearme.application.PaymentAppService;
import com.zionysus.dearme.node.PaymentVerifyNode;
import com.zionysus.dearme.north.acl.PaymentAcl;
import com.zionysus.dearme.north.acl.dto.ApiError;
import com.zionysus.dearme.north.acl.dto.PaymentRequest;
import com.zionysus.dearme.north.acl.dto.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付 HTTP 入站适配（北向 web adapter）。
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentAppService paymentAppService;
    private final PaymentAcl paymentAcl;

    @PostMapping
    public ResponseEntity<?> pay(@Valid @RequestBody PaymentRequest request) {
        PaymentAcl.PaymentRequestCmd command = paymentAcl.toCmd(request);
        PaymentVerifyNode.PaymentResult result = paymentAppService.pay(command.getSessionId(), command.getAmountCents());
        if (result == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "支付被拒绝（session 不存在或状态非法）", command.getSessionId()));
        }
        return ResponseEntity.ok(paymentAcl.toResponse(result));
    }
}
