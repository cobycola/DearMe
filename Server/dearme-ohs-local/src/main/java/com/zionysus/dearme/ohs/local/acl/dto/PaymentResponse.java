package com.zionysus.dearme.ohs.local.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String sessionId;
    private String status;
    private boolean sessionUnlocked;
    private boolean idempotentHit;
}