package com.zionysus.dearme.acl.infra.persistence.jdbc;

import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.session.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Session 聚合根的 jsonb 中间 DTO（南向 adapter 内部用）。
 * enum 一律以 String 还原，避开 enum 反序列化惊喜。
 *
 * 领域类零感知持久化——Session 不引用本类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionRecord {

    private String id;
    private String topicId;
    private Instant createdAt;
    private String status;
    private List<Answer> answers;
    private String reportContent;
    private Instant expiredAt;

    public SessionStatus statusEnum() {
        return SessionStatus.valueOf(status);
    }
}