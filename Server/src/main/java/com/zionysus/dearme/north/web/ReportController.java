package com.zionysus.dearme.north.web;

import com.zionysus.dearme.application.ReportAppService;
import com.zionysus.dearme.application.SessionAppService;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.north.acl.dto.ApiError;
import com.zionysus.dearme.north.acl.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报告 HTTP 入站适配（北向 web adapter）。
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ReportController {

    private final ReportAppService reportAppService;
    private final SessionAppService sessionAppService;

    @PostMapping("/{id}/report")
    public ResponseEntity<?> report(@PathVariable String id) {
        String markdown = reportAppService.generateReport(id);
        if (markdown == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "当前状态不允许生成报告", id));
        }
        Session session = sessionAppService.require(id);
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("NOT_FOUND", "session 不存在", id));
        }
        return ResponseEntity.ok(new ReportResponse(session.getId(), session.getStatus().name(), markdown));
    }
}