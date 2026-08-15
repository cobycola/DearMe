// 顶层状态机：IDLE -> CREATED -> PAID -> ASKING -> DONE -> REPORT_READY
// ERROR 为 overlay（不改变 stage），可回前一 stage 重试。
// sessionId + question + 进度 持久化 localStorage，刷新续程。
// 关键约束：后端无取当前题面的恢复接口，ASKING 中途刷新只能靠本地缓存的 question 续答，
// 不能重发 firstQuestion（PAID 之后会 409）。

const STORAGE_KEY = "dearme:session";

export const STAGE = {
  IDLE: "IDLE",
  CREATED: "CREATED",
  PAID: "PAID",
  ASKING: "ASKING",
  DONE: "DONE",
  REPORT_READY: "REPORT_READY",
};

export const initialState = {
  stage: STAGE.IDLE,
  sessionId: null,
  topicId: "anime-character",
  question: null, // {id, prompt, options}
  answeredCount: 0,
  topCandidateProbability: null,
  reportMarkdown: null,
  loading: false,
  pending: null, // 标识当前请求类型，按钮 disable 用
  error: null,
};

// actions
export const A = {
  START: "START", // 开始请求（{pending}）
  OK_SESSION: "OK_SESSION",
  OK_PAID: "OK_PAID",
  OK_QUESTION: "OK_QUESTION", // 首题或答题后拿到新题
  OK_DONE: "OK_DONE", // 答题完成，待取报告
  OK_REPORT: "OK_REPORT",
  ERR: "ERR", // {error}
  RETRY: "RETRY", // 清 error 重试
  RESET: "RESET", // 回 IDLE，清 localStorage
  RESTORE: "RESTORE", // 从 localStorage 恢复
};

export function reducer(state, action) {
  switch (action.type) {
    case A.START:
      return { ...state, loading: true, pending: action.pending || true, error: null };
    case A.OK_SESSION:
      return { ...state, loading: false, pending: null, stage: STAGE.CREATED, sessionId: action.sessionId };
    case A.OK_PAID:
      return { ...state, loading: false, pending: null, stage: STAGE.PAID };
    case A.OK_QUESTION: {
      const { response } = action.payload;
      return {
        ...state,
        loading: false,
        pending: null,
        stage: STAGE.ASKING,
        question: response.nextQuestion,
        answeredCount: response.answeredCount,
        topCandidateProbability: response.topCandidateProbability ?? null,
        error: null,
      };
    }
    case A.OK_DONE:
      return {
        ...state,
        loading: false,
        pending: null,
        stage: STAGE.DONE,
        question: null,
        answeredCount: action.payload.answeredCount,
        topCandidateProbability: action.payload.topCandidateProbability ?? null,
      };
    case A.OK_REPORT:
      return { ...state, loading: false, pending: null, stage: STAGE.REPORT_READY, reportMarkdown: action.reportMarkdown, error: null };
    case A.ERR:
      return { ...state, loading: false, pending: null, error: action.error };
    case A.RETRY:
      return { ...state, error: null };
    case A.RESTORE:
      return action.state ? { ...initialState, ...action.state, loading: false, pending: null, error: null } : initialState;
    case A.RESET:
      return { ...initialState };
    default:
      return state;
  }
}

// localStorage 持久化字段
function persistable(state) {
  return {
    sessionId: state.sessionId,
    stage: state.stage,
    topicId: state.topicId,
    question: state.question,
    answeredCount: state.answeredCount,
    topCandidateProbability: state.topCandidateProbability,
    reportMarkdown: state.reportMarkdown,
  };
}

export function saveSession(state) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(persistable(state)));
  } catch {
    // ignore quota / privacy mode
  }
}

export function loadSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== "object" || !parsed.sessionId) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function clearSession() {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {
    // ignore
  }
}