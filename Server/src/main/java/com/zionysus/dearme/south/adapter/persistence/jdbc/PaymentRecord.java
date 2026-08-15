package com.zionysus.dearme.south.adapter.persistence.jdbc;

import com.zionysus.dearme.domain.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Payment 聚合根的 jsonb 中间 DTO（南向 adapter 内部用）。
 * enum 以 String 还原。领域类零感知持久化。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {

    private String id;
    private String sessionId;
    private long amountCents;
    private Instant createdAt;
    private String status;
    private String externalTxnId;
    private String failureReason;

    public PaymentStatus statusEnum() {
        return PaymentStatus.valueOf(status);
    }
}