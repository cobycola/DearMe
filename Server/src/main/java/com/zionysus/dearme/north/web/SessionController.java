package com.zionysus.dearme.north.web;

import com.zionysus.dearme.application.SessionAppService;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.north.acl.SessionAcl;
import com.zionysus.dearme.north.acl.dto.AnswerResultView;
import com.zionysus.dearme.north.acl.dto.ApiError;
import com.zionysus.dearme.north.acl.dto.CreateSessionRequest;
import com.zionysus.dearme.north.acl.dto.CreateSessionResponse;
import com.zionysus.dearme.north.acl.dto.NextQuestionResponse;
import com.zionysus.dearme.north.acl.dto.SubmitAnswerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Session HTTP 入站适配（北向 web adapter 一种实现）。
 *
 * 仅做 HTTP 解析 + 调 ACL 转换 + 调 AppService 编排，不写业务。
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionAppService sessionAppService;
    private final SessionAcl sessionAcl;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateSessionRequest req) {
        var session = sessionAppService.create(sessionAcl.topicIdOf(req));
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("UNKNOWN_TOPIC", "未知主题", sessionAcl.topicIdOf(req)));
        }
        return ResponseEntity.ok(sessionAcl.toCreate(session));
    }

    @PostMapping("/{id}/first-question")
    public ResponseEntity<?> firstQuestion(@PathVariable String id) {
        var r = sessionAppService.firstQuestion(id);
        var s = sessionAppService.require(id);
        if (r == null || s == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "首题非法（session 不存在或非 PAID）", id));
        }
        var view = sessionAcl.toAnswerResult(s, r, true);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<?> submitAnswer(@PathVariable String id,
                                          @Valid @RequestBody SubmitAnswerRequest req) {
        var r = sessionAppService.submitAnswer(id, req.getQuestionId(), req.getOptionIndex());
        var s = sessionAppService.require(id);
        if (r == null || s == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "提交答案非法（session 不存在或非 ASKING）", id));
        }
        var view = sessionAcl.toAnswerResult(s, r, false);
        return ResponseEntity.ok(view);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> current(@PathVariable String id) {
        // 仅返回 session 当前已答进度（不返回题面，避免与 firstQuestion/answers 重复推题）
        var s = sessionAppService.require(id);
        if (s == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("NOT_FOUND", "session 不存在", id));
        }
        return ResponseEntity.ok(new NextQuestionResponse(null, s.getAnswers().size(),
                s.getStatus().name().equals("ANSWERED_ALL") || s.getStatus().name().equals("REPORT_READY"),
                null));
    }
}