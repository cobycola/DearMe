package com.zionysus.dearme.domain.session;

/**
 * Session 状态转换规则（领域服务）。
 *
 * 集中所有 guard 方法，保证 state 转换合法、可单测。
 * 不引入 Spring StateMachine 框架（过重）。
 */
public final class SessionTransition {

    private SessionTransition() {}

    public static void assertCanPay(Session s) {
        require(s, SessionStatus.CREATED, "未在 CREATED 状态，不允许支付");
    }

    public static void assertCanAnswer(Session s) {
        if (s.getStatus() != SessionStatus.PAID
                && s.getStatus() != SessionStatus.ASKING) {
            throw new IllegalStateException("当前状态 " + s.getStatus() + " 不允许答题");
        }
    }

    public static void assertCanRequestReport(Session s) {
        if (s.getStatus() != SessionStatus.ASKING
                && s.getStatus() != SessionStatus.ANSWERED_ALL
                && s.getStatus() != SessionStatus.REPORT_READY) {
            throw new IllegalStateException("当前状态 " + s.getStatus() + " 不允许请求报告");
        }
    }

    public static void markPaid(Session s) {
        assertCanPay(s);
        s.setStatus(SessionStatus.PAID);
    }

    public static void markAsking(Session s) {
        if (s.getStatus() != SessionStatus.PAID && s.getStatus() != SessionStatus.ASKING) {
            throw new IllegalStateException("仅 PAID/ASKING 可进入 ASKING");
        }
        s.setStatus(SessionStatus.ASKING);
    }

    public static void markAllAnswered(Session s) {
        if (s.getStatus() != SessionStatus.ASKING) {
            throw new IllegalStateException("仅 ASKING 可进入 ANSWERED_ALL");
        }
        s.setStatus(SessionStatus.ANSWERED_ALL);
    }

    public static void markReportReady(Session s) {
        if (s.getStatus() != SessionStatus.ASKING
                && s.getStatus() != SessionStatus.ANSWERED_ALL) {
            throw new IllegalStateException("仅 ASKING/ANSWERED_ALL 可进入 REPORT_READY");
        }
        s.setStatus(SessionStatus.REPORT_READY);
    }

    private static void require(Session s, SessionStatus expected, String msg) {
        if (s.getStatus() != expected) {
            throw new IllegalStateException(msg + "（当前: " + s.getStatus() + "）");
        }
    }
}