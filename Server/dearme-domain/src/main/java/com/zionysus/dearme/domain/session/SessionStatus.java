package com.zionysus.dearme.domain.session;

/**
 * Session 状态机。
 *
 * CREATED → PAID → ASKING → ANSWERED_ALL → REPORT_READY → EXPIRED
 *
 * 付费前置：CREATED 后第一件事付费，PAID 才进 ASKING。
 * 手写 enum + SessionTransition 集中 guard，不引入 Spring StateMachine 框架。
 */
public enum SessionStatus {
    CREATED,
    PAID,
    ASKING,
    ANSWERED_ALL,
    REPORT_READY,
    EXPIRED
}