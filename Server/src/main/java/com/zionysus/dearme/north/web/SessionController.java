package com.zionysus.dearme.north.web;

import com.zionysus.dearme.application.SessionAppService;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.node.InferenceNode;
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
    public ResponseEntity<?> create(@Valid @RequestBody CreateSessionRequest request) {
        Session session = sessionAppService.create(sessionAcl.topicIdOf(request));
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("UNKNOWN_TOPIC", "未知主题", sessionAcl.topicIdOf(request)));
        }
        return ResponseEntity.ok(sessionAcl.toCreate(session));
    }

    @PostMapping("/{id}/first-question")
    public ResponseEntity<?> firstQuestion(@PathVariable String id) {
        InferenceNode.Result result = sessionAppService.firstQuestion(id);
        Session session = sessionAppService.require(id);
        if (result == null || session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "首题非法（session 不存在或非 PAID）", id));
        }
        AnswerResultView view = sessionAcl.toAnswerResult(session, result, true);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<?> submitAnswer(@PathVariable String id,
                                          @Valid @RequestBody SubmitAnswerRequest request) {
        InferenceNode.Result result = sessionAppService.submitAnswer(id, request.getQuestionId(), request.getOptionIndex());
        Session session = sessionAppService.require(id);
        if (result == null || session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "提交答案非法（session 不存在或非 ASKING）", id));
        }
        AnswerResultView view = sessionAcl.toAnswerResult(session, result, false);
        return ResponseEntity.ok(view);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> current(@PathVariable String id) {
        // 仅返回 session 当前已答进度（不返回题面，避免与 firstQuestion/answers 重复推题）
        Session session = sessionAppService.require(id);
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("NOT_FOUND", "session 不存在", id));
        }
        return ResponseEntity.ok(new NextQuestionResponse(null, session.getAnswers().size(),
                session.getStatus().name().equals("ANSWERED_ALL") || session.getStatus().name().equals("REPORT_READY"),
                null));
    }
}
