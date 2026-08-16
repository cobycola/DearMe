package com.zionysus.dearme.ohs.local.web;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.ohs.local.acl.dto.ApiError;
import com.zionysus.dearme.ohs.local.acl.dto.ReportResponse;
import com.zionysus.dearme.ohs.local.appservice.QuizAppService;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
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

    private final QuizAppService quizAppService;

    @PostMapping("/{id}/report")
    public ResponseEntity<?> report(@PathVariable String id) {
        SessionFlowContext context = quizAppService.report(id);
        if (!context.isOk()) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "当前状态不允许生成报告", id));
        }
        Session session = context.getSession();
        return ResponseEntity.ok(new ReportResponse(
                session.getId(), session.getStatus().name(), context.getReportMarkdown()));
    }
}
