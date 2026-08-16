package com.zionysus.dearme.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用结果载体（跨层共享）。
 *
 * 流程编排层（ohs-local Node 管道）用它标记某节点执行失败与否，
 * 北向 web 层据 {@link #getCode()} 映射 HTTP 错误响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private boolean success = true;
    private String code;
    private String message;

    public static Result ok() {
        return new Result(true, null, null);
    }

    public static Result fail(String code, String message) {
        return new Result(false, code, message);
    }
}
