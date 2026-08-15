package com.zionysus.dearme.north.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private String detail;

    public static ApiError of(String code, String message, String detail) {
        return new ApiError(code, message, detail);
    }
}