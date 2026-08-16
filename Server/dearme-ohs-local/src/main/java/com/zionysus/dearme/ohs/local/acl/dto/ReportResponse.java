package com.zionysus.dearme.ohs.local.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private String sessionId;
    private String status;
    private String reportMarkdown;
}