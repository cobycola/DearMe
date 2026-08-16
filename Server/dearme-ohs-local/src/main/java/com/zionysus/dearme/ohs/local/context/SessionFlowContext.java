package com.zionysus.dearme.ohs.local.context;

import com.zionysus.dearme.common.model.Result;
import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;
import com.zionysus.dearme.domain.session.Session;
import lombok.Data;

/**
 * quiz 主链路流程编排上下文（对应 pay-settle-front 的 CashierAppContext / PreDataContext）。
 *
 * 贯穿「创建 → 付费 → 首题 → 答题 → 报告」的每个 Node 管道：
 *   - 入参：上游下发的请求字段
 *   - 状态：{@link Result} 标记本流程成败，北向 web 层据 code 映射 HTTP 错误
 *   - 出参：各 Node 写入的中间/最终结果
 *
 * 单个可变对象随管道流转，节点间不传参，只读写本 context。
 */
@Data
public class SessionFlowContext {

    /* ------------------------------ 入参 ------------------------------ */
    private String topicId;
    private String sessionId;
    private String questionId;
    private int optionIndex;
    private long amountCents;

    /* ------------------------------ 流程状态 ------------------------------ */
    private Result result = Result.ok();

    /* ------------------------------ 中间/出参 ------------------------------ */
    private Session session;
    private Question nextQuestion;
    private InferenceSummary summary;
    private Payment payment;
    private boolean idempotentHit;
    private boolean sessionUnlocked;
    private String reportMarkdown;

    public boolean isOk() {
        return result != null && result.isSuccess();
    }
}
