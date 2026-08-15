package com.zionysus.dearme.south.adapter.ai.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Worker 调用结果。成功返回结构化对象，失败携带原因。
 *
 * 设计为「结果对象」而非抛异常：
 *   - 便于业务 adapter 看到 failure 后切降级路径（不抛 = 控制流不被打断）
 *   - 但业务 adapter 也可以选择 .orElseThrow() 向上抛
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkerResult<T> {

    private T value;
    private String failureReason;
    private boolean success;

    public static <T> AiWorkerResult<T> ok(T value) {
        return new AiWorkerResult<>(value, null, true);
    }

    public static <T> AiWorkerResult<T> fail(String reason) {
        return new AiWorkerResult<>(null, reason, false);
    }

    public T orElseThrow() {
        if (!success) {
            throw new AiWorkerException(failureReason);
        }
        return value;
    }

    public static class AiWorkerException extends RuntimeException {
        public AiWorkerException(String msg) {
            super(msg);
        }
    }
}