package com.zionysus.dearme.domain.payment;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment 聚合根。MVP 内存态。
 */
public class Payment {

    private final String id;
    private final String sessionId;
    private final long amountCents;
    private final Instant createdAt;
    private PaymentStatus status;
    private String externalTxnId;   // 模拟支付网关返回的交易号
    private String failureReason;

    public Payment(String sessionId, long amountCents) {
        this.id = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        this.amountCents = amountCents;
        this.createdAt = Instant.now();
        this.status = PaymentStatus.PENDING;
    }

    private Payment(String id, String sessionId, long amountCents, Instant createdAt,
                    PaymentStatus status, String externalTxnId, String failureReason) {
        this.id = id;
        this.sessionId = sessionId;
        this.amountCents = amountCents;
        this.createdAt = createdAt;
        this.status = status;
        this.externalTxnId = externalTxnId;
        this.failureReason = failureReason;
    }

    /**
     * 从持久化存储重建聚合根。仅供 south adapter 反序列化用；
     * 业务构造请走 {@link #Payment(String, long)}。
     */
    public static Payment reconstitute(String id, String sessionId, long amountCents,
                                       Instant createdAt, PaymentStatus status,
                                       String externalTxnId, String failureReason) {
        return new Payment(id, sessionId, amountCents, createdAt, status, externalTxnId, failureReason);
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public long getAmountCents() { return amountCents; }
    public Instant getCreatedAt() { return createdAt; }
    public PaymentStatus getStatus() { return status; }
    public String getExternalTxnId() { return externalTxnId; }
    public String getFailureReason() { return failureReason; }

    public void markSuccess(String externalTxnId) {
        this.status = PaymentStatus.SUCCESS;
        this.externalTxnId = externalTxnId;
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void markTimeout() {
        this.status = PaymentStatus.TIMEOUT;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.SUCCESS;
    }
}