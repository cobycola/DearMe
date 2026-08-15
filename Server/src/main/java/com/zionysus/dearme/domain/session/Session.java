package com.zionysus.dearme.domain.session;

import com.zionysus.dearme.domain.question.Answer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Session 聚合根。
 *
 * 内存态（MVP 不持久化）。承载用户在某一主题下的一次测试全部状态：
 * 题目进度、答案、最终报告。状态由 SessionTransition 守护。
 */
public class Session {

    private final String id;
    private final String topicId;
    private final Instant createdAt;
    private SessionStatus status;
    private final List<Answer> answers = new ArrayList<>();
    private String reportContent;
    private Instant expiredAt;

    public Session(String topicId) {
        this.id = UUID.randomUUID().toString();
        this.topicId = topicId;
        this.createdAt = Instant.now();
        this.status = SessionStatus.CREATED;
    }

    private Session(String id, String topicId, Instant createdAt,
                    SessionStatus status, java.util.List<Answer> answers,
                    String reportContent, Instant expiredAt) {
        this.id = id;
        this.topicId = topicId;
        this.createdAt = createdAt;
        this.status = status;
        this.answers.addAll(answers);
        this.reportContent = reportContent;
        this.expiredAt = expiredAt;
    }

    /**
     * 从持久化存储重建聚合根。仅供 south adapter 反序列化用；
     * 业务构造请走 {@link #Session(String)}。
     */
    public static Session reconstitute(String id, String topicId, Instant createdAt,
                                       SessionStatus status, java.util.List<Answer> answers,
                                       String reportContent, Instant expiredAt) {
        return new Session(id, topicId, createdAt, status, answers, reportContent, expiredAt);
    }

    public String getId() { return id; }
    public String getTopicId() { return topicId; }
    public Instant getCreatedAt() { return createdAt; }
    public SessionStatus getStatus() { return status; }
    void setStatus(SessionStatus s) { this.status = s; }
    public List<Answer> getAnswers() { return Collections.unmodifiableList(answers); }
    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }
    public Instant getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Instant expiredAt) { this.expiredAt = expiredAt; }

    public void addAnswer(Answer a) {
        SessionTransition.assertCanAnswer(this);
        answers.add(a);
    }
}