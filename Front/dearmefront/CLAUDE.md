# DearMe 前端规范（手机网页端）

> 给后端 `D:\DEARME\SERVER`（Spring Boot 4.1 / DDD 六边形 / 心理测试 SaaS，MVP 单主题「看看我像哪个动漫人物」）定制的手机网页端。
> MVP 主链路：主题页 → 付费 → 答题 → 报告。

## 语言

沟通用中文；代码/变量/命令用英文。

## 技术栈

- CRA（react-scripts 5）+ React 19。
- **唯一额外运行依赖**：`react-markdown`（渲染后端下发的报告 Markdown）。
- **不引入**：路由库、状态管理库、HTTP 库、CSS 框架。主链路四屏是同一会话的线性阶段，用顶层 `useReducer` 驱动 stage 切换即可；HTTP 用原生 fetch 封装；样式用原生 CSS + CSS 变量 + CSS Modules。引入上述库前先问，不要默认加。
- 联调：`package.json` 的 `proxy: http://localhost:8080`，dev 下 `/api/*` 走代理免 CORS。**前端 fetch 一律相对路径 `/api/...`**，勿写绝对后端地址。

## 目录约定

```
src/
├── api/        后端接口封装（client=fetch 封装，sessionApi=各接口）
├── config/     硬编码前端常量（topics 等，后端缺接口时培上）
├── state/      reducer + thunk 式 dispatch 工厂 + localStorage 恢复
├── theme/      tokens.css 设计 token
├── components/  复用 UI 元件
└── views/      与 stage 一一对应的四屏
```

- 组件 PascalCase，文件与组件同名；CSS Modules 命名 `Foo.module.css`，与 `Foo.js` 同目录。
- 包：全小写单数。

## 样式约定

- 设计 token 全在 `src/theme/tokens.css :root`，组件只引用 `var(--token)`，**禁止硬编码颜色/尺寸字面量**。
- 暗色克制极简：唯一强调色 `--accent`；几乎不用阴影，靠 surface 色差分层；过渡 ≤200ms opacity/transform，不引动效库。
- 移动安全区：可点高度 ≥44px（按钮 52px），正文 ≥16px，引 `env(safe-area-inset-*)`。

## 状态管理

- 顶层 `useReducer` 驱动 stage：`IDLE → CREATED → PAID → ASKING → DONE → REPORT_READY`。ERROR 为 overlay（不改 stage），可回前一 stage 重试。
- 异步接口写成手写「dispatch START → 调用 → 成功/失败」流程，不引 thunk/RTK/saga。
- 关键阶段链式续接：付费成功立即 `first-question`；答完立即 `report`，不让用户多点一次。
- `sessionId + stage + question + 进度 + reportMarkdown` 持久化 localStorage（key `dearme:session`），刷新续程。
  - **真实约束**：后端无取当前题面的恢复接口（`GET /api/sessions/{id}` 故意只返回进度不返回题面；`first-question` 仅 PAID 可调，ASKING 中途调会 409）。→ ASKING 中途刷新只能靠**本地缓存的 question 续答**，不能重发 first-question。
  - 任意恢复接口返回 404/409 → `clearSession` 回 IDLE。

## API 契约入口

`src/api/sessionApi.js` 顶部注释列 5 接口及请求/响应结构。错误体后端约定 `{code, message, detail}`，非 2xx 由 `src/api/client.js` 的 `request()` 统一解析为 `ApiError`（含 `.message`），view 层只展示 `.message`。状态码：404/400/409/500。

## 克制原则（防过度设计）

- 策略/端口/工厂只在真有多替换需求处用；零散一两行的支线不抽接口。
- 默认不写注释；仅在 WHY 非显然时写一行中文解释。
- 不为用模式而用模式；单链路 MVP 不预支未来多 Topic 商城结构。

## 安全

- 本前端不持任何密钥（`DEEPSEEK_API_KEY` 仅后端需要）。
- 密钥/token 不进代码、不进 commit、不进日志。

## 验证命令

- `npm start` —— dev server 3000，代理后端 8080，改即热更。
- `npm run build` —— 构建到 `build/`，必须无报错。改完主动跑。
- `npm test` —— 轻量 smoke（默认 CRA watch 模式）。

## 端到端冒烟

1. 起后端：后端根设 `DEEPSEEK_API_KEY` 后 `mvn spring-boot:run`（端口 8080）。
2. 起前端：`npm start`，浏览器开 `http://localhost:3000`，DevTools 设备模拟 iPhone 12（390x844）。
3. 主链路：开始 → `POST /api/sessions`（CREATED）→ 确认支付 → `POST /api/payments`（sessionUnlocked）→ 自动 `first-question` → 答 10 题 → `nextQuestion===null && done` → 自动 `report` → Markdown 渲染。
4. 恢复：答题中刷新靠本地 question 续答；报告页刷新重拉 report。
5. 错误：停后端再操作 → ErrorBanner 显示后端 message，非白屏。

## 红线（即使 auto-accept 也先问我）

- 修改后端代码（本前端走代理方案，理论上不动后端；如需后端配合接口变化先问）
- 修改本 CLAUDE.md 的核心约定（技术栈选型/状态策略/恢复约束）
- 安装新的全局依赖
- 接真实支付替换 Mock
- 提交代码含密钥/.env