package com.zionysus.dearme.ohs.local.web;

import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionStatus;
import com.zionysus.dearme.ohs.local.acl.SessionAcl;
import com.zionysus.dearme.ohs.local.acl.dto.ApiError;
import com.zionysus.dearme.ohs.local.acl.dto.CreateSessionRequest;
import com.zionysus.dearme.ohs.local.acl.dto.NextQuestionResponse;
import com.zionysus.dearme.ohs.local.acl.dto.SubmitAnswerRequest;
import com.zionysus.dearme.ohs.local.appservice.QuizAppService;
import com.zionysus.dearme.ohs.local.context.SessionFlowContext;
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
 * Session HTTP 入站适配（北向 web adapter）。
 *
 * 仅做 HTTP 解析 + 调 ACL 转换 + 调 AppService 串链 Node 管道，不写业务。
 * 失败走 context.result（业务非法不抛异常），映射 4xx ApiError。
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final QuizAppService quizAppService;
    private final SessionAcl sessionAcl;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateSessionRequest request) {
        String topicId = sessionAcl.topicIdOf(request);
        SessionFlowContext context = quizAppService.create(topicId);
        if (!context.isOk()) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("UNKNOWN_TOPIC", "未知主题", topicId));
        }
        return ResponseEntity.ok(sessionAcl.toCreate(context));
    }

    @PostMapping("/{id}/first-question")
    public ResponseEntity<?> firstQuestion(@PathVariable String id) {
        SessionFlowContext context = quizAppService.firstQuestion(id);
        if (!context.isOk()) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "首题非法（session 不存在或非 PAID）", id));
        }
        return ResponseEntity.ok(sessionAcl.toAnswerResult(context, true));
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<?> submitAnswer(@PathVariable String id,
                                          @Valid @RequestBody SubmitAnswerRequest request) {
        SessionFlowContext context = quizAppService.submitAnswer(
                id, request.getQuestionId(), request.getOptionIndex());
        if (!context.isOk()) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("STATE_CONFLICT", "提交答案非法（session 不存在或非 ASKING）", id));
        }
        return ResponseEntity.ok(sessionAcl.toAnswerResult(context, false));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> current(@PathVariable String id) {
        // 仅返回 session 当前已答进度（不返回题面，避免与 firstQuestion/answers 重复推题）
        Session session = quizAppService.require(id);
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiError.of("NOT_FOUND", "session 不存在", id));
        }
        boolean done = session.getStatus() == SessionStatus.ANSWERED_ALL
                || session.getStatus() == SessionStatus.REPORT_READY;
        return ResponseEntity.ok(new NextQuestionResponse(
                null, session.getAnswers().size(), done, null));
    }
}
