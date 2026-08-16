package com.zionysus.dearme.acl.ports;

import com.zionysus.dearme.domain.payment.Payment;

/**
 * 支付网关端口（南向 outbound port）。
 *
 * 当前实现：MockPaymentAdapter —— 模拟成/败/超时三态，方便测试。
 * 扩展点：接真实支付网关（微信/支付宝/Stripe 等），踩「公开发布到生产」红线需用户亲自接入。
 *
 * 设计为同步调用：MVP 用未来式简单支付流程，发起即返回结果。
 * 真实异步网关可以在此接口基础上扩展 queryByTxnId 方法做轮询/回调。
 */
public interface PaymentGatewayPort {

    /**
     * 发起支付。实现负责：
     *   - 调用网关（mock 或真实）
     *   - 把结果回填到 payment（markSuccess/markFailed/markTimeout）
     *   - 返回更新后的 payment
     */
    Payment charge(Payment payment);
}