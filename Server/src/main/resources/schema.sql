-- DearMe MVP schema。spring-jdbc + jsonb 整列存聚合根。
-- 业务层经 SessionRepositoryPort / PaymentRepositoryPort 操作聚合根，仓储 adapter 负责整列 ↔ jsonb 序列化。

CREATE TABLE IF NOT EXISTS sessions (
    id         TEXT PRIMARY KEY,
    topic_id   TEXT      NOT NULL,
    status     TEXT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    body       JSONB     NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id         TEXT PRIMARY KEY,
    session_id TEXT      NOT NULL,
    status     TEXT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    body       JSONB     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payments_session ON payments (session_id);

-- D5 DB 兜底幂等：同一 session 最多一条 SUCCESS。并发两笔 SUCCESS 部分唯一索引拒绝，仓储回查既有成功单。
CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_success_per_session
    ON payments (session_id) WHERE status = 'SUCCESS';