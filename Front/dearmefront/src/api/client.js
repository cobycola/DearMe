// fetch 封装：超时 + 非2xx 解析后端 ApiError{code,message,detail} 抛 Error(message)
// 后端约定：非2xx 返回 {code,message,detail}（见 GlobalExceptionHandler）

export class ApiError extends Error {
  constructor(message, code, detail, status) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.detail = detail;
    this.status = status;
  }
}

const DEFAULT_TIMEOUT_MS = 20000;

export async function request(path, { method = "GET", body, timeoutMs = DEFAULT_TIMEOUT_MS } = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const res = await fetch(path, {
      method,
      headers: body !== undefined ? { "Content-Type": "application/json" } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
    const text = await res.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = { message: text };
      }
    }
    if (!res.ok) {
      const message =
        (data && typeof data === "object" && data.message) ||
        `请求失败（${res.status}）`;
      const code = data && data.code;
      const detail = data && data.detail;
      throw new ApiError(message, code, detail, res.status);
    }
    return data;
  } finally {
    clearTimeout(timer);
  }
}