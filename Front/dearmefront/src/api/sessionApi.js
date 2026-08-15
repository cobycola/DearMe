// 后端 API 契约（Spring Boot 4.1 / DDD 六边形，端口 8080，前端走 proxy）
//
// 1. POST /api/sessions              body {topicId}                                 -> {sessionId, status, topicId}
// 2. POST /api/payments               body {sessionId, amountCents}                 -> {paymentId, sessionId, status, sessionUnlocked, idempotentHit}
// 3. POST /api/sessions/{id}/first-question                                          -> {response:{nextQuestion,answeredCount,done,topCandidateProbability}, isFirst}
// 4. POST /api/sessions/{id}/answers  body {questionId, optionIndex:0~3}            -> AnswerResultView（同上结构，isFirst:false；nextQuestion===null 即答完）
// 5. POST /api/sessions/{id}/report                                                  -> {sessionId, status, reportMarkdown}
//
// 非通用响应包装在 AnswerResultView；首题与答题用同一结构，靠 isFirst 区分。
// reportMarkdown 是后端下发的 Markdown 字符串。

import { request } from "./client";

export function createSession(topicId) {
  return request("/api/sessions", { method: "POST", body: { topicId } });
}

export function pay(sessionId, amountCents) {
  return request("/api/payments", { method: "POST", body: { sessionId, amountCents } });
}

export function firstQuestion(sessionId) {
  return request(`/api/sessions/${sessionId}/first-question`, { method: "POST" });
}

export function submitAnswer(sessionId, questionId, optionIndex) {
  return request(`/api/sessions/${sessionId}/answers`, {
    method: "POST",
    body: { questionId, optionIndex },
  });
}

export function fetchReport(sessionId) {
  return request(`/api/sessions/${sessionId}/report`, { method: "POST" });
}