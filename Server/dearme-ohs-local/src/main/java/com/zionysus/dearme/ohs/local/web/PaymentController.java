package com.zionysus.dearme.ohs.local.web;

import com.zionysus.dearme.ohs.local.acl.PaymentAcl;
import com.zionysus.dearme.ohs.local.acl.dto.ApiError;
import com.zionysus.dearme.ohs.local.acl.dto.PaymentRequest;
import com.zionysus.dearme.ohs.local.appservice.QuizAppService;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
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

    private final QuizAppService quizAppService;
    private final PaymentAcl paymentAcl;

    @PostMapping
    public ResponseEntity<?> pay(@Valid @RequestBody PaymentRequest request) {
        PaymentAcl.PaymentRequestCmd command = paymentAcl.toCmd(request);
        SessionFlowContext context = quizAppService.pay(command.getSessionId(), command.getAmountCents());
        if (!context.isOk()) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "支付被拒绝（session 不存在或状态非法）", command.getSessionId()));
        }
        return ResponseEntity.ok(paymentAcl.toResponse(context));
    }
}
