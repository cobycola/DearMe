package com.zionysus.dearme.domain.session;

/**
 * Session 状态转换规则（领域服务）。
 *
 * 集中所有 guard 方法，保证 state 转换合法、可单测。
 * 不引入 Spring StateMachine 框架（过重）。
 */
public final class SessionTransition {

    private SessionTransition() {}

    public static void assertCanPay(Session session) {
        require(session, SessionStatus.CREATED, "未在 CREATED 状态，不允许支付");
    }

    public static void assertCanAnswer(Session session) {
        if (session.getStatus() != SessionStatus.PAID
                && session.getStatus() != SessionStatus.ASKING) {
            throw new IllegalStateException("当前状态 " + session.getStatus() + " 不允许答题");
        }
    }

    public static void assertCanRequestReport(Session session) {
        if (session.getStatus() != SessionStatus.ASKING
                && session.getStatus() != SessionStatus.ANSWERED_ALL
                && session.getStatus() != SessionStatus.REPORT_READY) {
            throw new IllegalStateException("当前状态 " + session.getStatus() + " 不允许请求报告");
        }
    }

    public static void markPaid(Session session) {
        assertCanPay(session);
        session.setStatus(SessionStatus.PAID);
    }

    public static void markAsking(Session session) {
        if (session.getStatus() != SessionStatus.PAID && session.getStatus() != SessionStatus.ASKING) {
            throw new IllegalStateException("仅 PAID/ASKING 可进入 ASKING");
        }
        session.setStatus(SessionStatus.ASKING);
    }

    public static void markAllAnswered(Session session) {
        if (session.getStatus() != SessionStatus.ASKING) {
            throw new IllegalStateException("仅 ASKING 可进入 ANSWERED_ALL");
        }
        session.setStatus(SessionStatus.ANSWERED_ALL);
    }

    public static void markReportReady(Session session) {
        if (session.getStatus() != SessionStatus.ASKING
                && session.getStatus() != SessionStatus.ANSWERED_ALL) {
            throw new IllegalStateException("仅 ASKING/ANSWERED_ALL 可进入 REPORT_READY");
        }
        session.setStatus(SessionStatus.REPORT_READY);
    }

    private static void require(Session session, SessionStatus expected, String msg) {
        if (session.getStatus() != expected) {
            throw new IllegalStateException(msg + "（当前: " + session.getStatus() + "）");
        }
    }
}
