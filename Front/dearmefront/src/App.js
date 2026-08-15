import { useEffect, useReducer, useRef } from "react";
import TopicView from "./views/TopicView";
import PayView from "./views/PayView";
import QuizView from "./views/QuizView";
import ReportView from "./views/ReportView";
import Spinner from "./components/Spinner";
import {
  A,
  STAGE,
  initialState,
  reducer,
  loadSession,
  saveSession,
  clearSession,
} from "./state/sessionReducer";
import { createSession, pay, firstQuestion, submitAnswer, fetchReport } from "./api/sessionApi";

// 惰性初始化：从 localStorage 恢复会话
function init() {
  const saved = loadSession();
  if (!saved || !saved.stage) return initialState;
  // 恢复到 ASKING 靠本地 question 缓存；恢复到 DONE 需要重新拉报告。
  return { ...initialState, ...saved };
}

export default function App() {
  const [state, dispatch] = useReducer(reducer, undefined, init);
  const initRef = useRef(false);

  // 持久化：刻意只依赖可持久化字段，避免把 loading/error 等瞬态写回 localStorage
  useEffect(() => {
    if (state.stage === STAGE.IDLE) return;
    saveSession(state);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.stage, state.sessionId, state.question, state.answeredCount,
       state.topCandidateProbability, state.reportMarkdown]);

  // 恢复钩子：DONE 状态下自动拉报告
  useEffect(() => {
    if (initRef.current) return;
    initRef.current = true;
    if (state.stage === STAGE.DONE && state.sessionId) {
      loadReport(state.sessionId);
    }
  });

  async function startSession() {
    dispatch({ type: A.START, pending: "create" });
    try {
      const r = await createSession(state.topicId);
      dispatch({ type: A.OK_SESSION, sessionId: r.sessionId });
    } catch (e) {
      dispatch({ type: A.ERR, error: e.message });
    }
  }

  async function payAndStart() {
    dispatch({ type: A.START, pending: "pay" });
    try {
      const r = await pay(state.sessionId, Math.round(9.9 * 100));
      if (!r.sessionUnlocked) {
        dispatch({ type: A.ERR, error: "支付未成功，请重试" });
        return;
      }
      dispatch({ type: A.OK_PAID });
      // 链式调首题
      dispatch({ type: A.START, pending: "first" });
      try {
        const q = await firstQuestion(state.sessionId);
        dispatch({ type: A.OK_QUESTION, payload: q });
      } catch (e) {
        dispatch({ type: A.ERR, error: e.message });
      }
    } catch (e) {
      dispatch({ type: A.ERR, error: e.message });
    }
  }

  async function answer(questionId, optionIndex) {
    dispatch({ type: A.START, pending: "answer" });
    try {
      const r = await submitAnswer(state.sessionId, questionId, optionIndex);
      const resp = r.response;
      if (resp.nextQuestion && !resp.done) {
        dispatch({ type: A.OK_QUESTION, payload: r });
      } else {
        // 答完：resp.done === true, nextQuestion === null
        dispatch({ type: A.OK_DONE, payload: { answeredCount: resp.answeredCount, topCandidateProbability: resp.topCandidateProbability } });
        // 链式拉报告
        await loadReport(state.sessionId);
      }
    } catch (e) {
      dispatch({ type: A.ERR, error: e.message });
    }
  }

  async function loadReport(sessionId = state.sessionId) {
    dispatch({ type: A.START, pending: "report" });
    try {
      const r = await fetchReport(sessionId);
      dispatch({ type: A.OK_REPORT, reportMarkdown: r.reportMarkdown });
    } catch (e) {
      dispatch({ type: A.ERR, error: e.message });
    }
  }

  // 付费成功后首题失败时的恢复：重发 first-question（此时 session 仍 PAID）
  async function startQuizAfterPay() {
    dispatch({ type: A.START, pending: "first" });
    try {
      const q = await firstQuestion(state.sessionId);
      dispatch({ type: A.OK_QUESTION, payload: q });
    } catch (e) {
      dispatch({ type: A.ERR, error: e.message });
    }
  }

  function reset() {
    clearSession();
    dispatch({ type: A.RESET });
  }

  function backToTopic() {
    if (state.stage === STAGE.ASKING || state.stage === STAGE.DONE) {
      const ok = window.confirm("未完成测评将丢失进度，确定放弃？");
      if (!ok) return;
    }
    reset();
  }

  const { stage, loading, error } = state;

  const content = (() => {
    switch (stage) {
      case STAGE.IDLE:
        return (
          <TopicView
            topicId={state.topicId}
            loading={loading}
            error={error}
            onStart={startSession}
            onRetry={startSession}
          />
        );
      case STAGE.CREATED:
        return (
          <PayView
            topicId={state.topicId}
            sessionId={state.sessionId}
            loading={loading}
            error={error}
            onPay={payAndStart}
            onRetry={payAndStart}
            onBack={backToTopic}
          />
        );
      case STAGE.PAID:
        return (
          <QuizView
            question={state.question}
            answeredCount={state.answeredCount}
            topCandidateProbability={state.topCandidateProbability}
            loading={loading}
            error={error}
            onAnswer={answer}
            onBack={backToTopic}
            onRetry={startQuizAfterPay}
          />
        );
      case STAGE.ASKING:
        return (
          <QuizView
            question={state.question}
            answeredCount={state.answeredCount}
            topCandidateProbability={state.topCandidateProbability}
            loading={loading}
            error={error}
            onAnswer={answer}
            onBack={backToTopic}
          />
        );
      case STAGE.DONE:
        return (
          <ReportView
            reportMarkdown={null}
            loading={loading}
            error={error}
            onRetry={loadReport}
            onReset={reset}
          />
        );
      case STAGE.REPORT_READY:
        return (
          <ReportView
            reportMarkdown={state.reportMarkdown}
            loading={loading}
            error={error}
            onRetry={loadReport}
            onReset={reset}
          />
        );
      default:
        return (
          <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh" }}>
            <Spinner label="加载中…" />
          </div>
        );
    }
  })();

  return content;
}