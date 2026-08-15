# DearMe 项目规范

> AI agent 心理测试 SaaS。用户付费购买一轮 session，agent 动态出题、推理出结论、生成报告。
> MVP 单链路验证：只做"看看我像哪个动漫人物"一个主题，跑通「付费 → 答题 → 推理 → 报告」闭环。

## 业务模式

- **全付费 + Mock 预付费 + 不退款**。付费点前置——点主题后先付费，再开始答题。
- MVP 用 `MockPaymentAdapter`，预留真实网关接口（`PaymentGatewayPort`）。
- 不退款模型下，预期管理完全靠**产品页预览文案**。付费前必须让用户清楚购买内容。
- 这个组合验证不了真实付费转化率（Mock 下无真钱压力），MVP 跑通仅证明工程闭环正确。

## 技术栈

- Java 21 + Spring Boot 4.1 + Spring AI 2.0（DeepSeek starter）+ Lombok + commons-lang3
- 包根：`com.zionysus.dearme`
- 构建工具：Maven
- **Jackson 3**（Spring Boot 4 自带）：包名 `tools.jackson.databind.ObjectMapper`、`tools.jackson.core.type.TypeReference`；注解仍 `com.fasterxml.jackson.annotation`。注入 `ObjectMapper` bean，**不要 new**。

## 分层架构（DDD + 六边形 Hexagonal）

```
src/main/java/com/zionysus/dearme/
├── DearMeApplication.java
│
├── north/                          # 北向 · 入站驱动端
│   ├── acl/                          # 防腐层（DTO ↔ 领域命令转换）
│   │   ├── SessionAcl.java
│   │   ├── PaymentAcl.java
│   │   └── dto/                      # HTTP 请求/响应边界
│   └── web/                          # HTTP 入站 adapter
│       ├── *Controller.java
│       └── GlobalExceptionHandler.java
│
├── application/                     # 应用编排层（编排，不写业务）
│   └── *AppService.java
│
├── node/                           # NodeService（单一职责处理节点，被 AppService 串联）
│   └── *Node.java
│
├── domain/                          # 领域层（核心，不依赖任何外部）
│   ├── session/   # Session 聚合根、状态机（SessionTransition 纯领域服务）
│   ├── inference/  # 特征维度、候选画像、Topic、engine/、policy/
│   ├── payment/    # Payment 聚合根
│   ├── question/   # Question/Answer/QuestionType
│   └── report/     # InferenceSummary
│
├── south/                          # 南向 · 出站被驱动端
│   ├── port/                        # 端口接口（领域依赖的抽象契约）
│   └── adapter/                     # 同进程 adapter 实现
│       ├── persistence/             # JSON/内存仓储 adapter
│       ├── registry/                 # TopicRegistry 实现
│       ├── mock/                     # 支付 Mock adapter
│       └── ai/                       # AI adapter
│           ├── worker/                # AiWorker 统一抽象（屏蔽 ChatClient）
│           ├── LlmReportGeneratorAdapter.java（主，@Primary）
│           └── TemplateReportGeneratorAdapter.java（降级底线）
│
└── common/                         # 跨层共享小工具
    ├── id/IdGenerator.java
    └── time/TimeProvider.java
```

## 分层依赖方向（硬性约束）

1. **依赖单向**：`north → application → node → domain ← south`。反向禁止。
2. **domain 不依赖任何外部包**——包括 north/application/node/south。它是核心。
3. **port 由 domain 持有依赖关系**：端口接口物理放 `south/port/`，但语义上属 domain 想要的能力契约。domain 包通过 import 用它，adapter 实现它。
4. **推理引擎/策略属 domain**：`WeightedTraitEngine`、`Scorer`、`InformationGainPolicy` 是领域内算法，归 `domain/inference/{engine,policy}/`，不进 south（它们不调外部）。
5. **AI 调用经 AiWorker**：所有调 LLM 的业务 adapter（如报告生成）必须经 `AiWorker` 接口，不直接用 Spring AI `ChatClient`。换底层模型只改 Worker 实现，业务 adapter 不动。
6. **node 不互相依赖**：NodeService 节点之间通过 AppService 编排串联，禁止相互 import。
7. **common 防垃圾桶化**：拿不准放不放的，**不放 common**，宁可放具体模块。
8. **AppService 不写业务**：只编排、调 NodeService + 端口、做状态推进。具体处理逻辑必须放 NodeService 或 domain。

## 架构决策（落代码前必读）

### D1. 推理引擎 = 加权特征匹配 + 信息增益选题

- 不做贝叶斯。封闭空间下，加权匹配可单测、标注成本低，效果对得起 MVP。
- `domain/inference/engine/`：`Scorer`（高斯核候选排序）+ `WeightedTraitEngine`（实现 `InferenceEngine` 端口）。
- `domain/inference/policy/`：`InformationGainPolicy`（实现 `NextQuestionPolicy` 端口），按预期信息增益选题 + 同维度指数惩罚去重。
- 标注分层省工作量：题库只标"题→特征维度"映射，候选只标维度值。**不直接标题×候选矩阵**。维度 8 个（可扩至 8~12）。

### D2. LLM 只用在最终报告生成

- 出题全走结构化题库（D1），LLM 不介入出题。
- 首题不用 LLM 破冰，按 `firstQuestion` 取信息增益最高的题开场。
- 报告生成走 `LlmReportGeneratorAdapter`：
  - 经 `AiWorker.execute(req)` 调 Spring AI `ChatClient.entity(ReportDto.class)` 强转结构化输出
  - **白名单校验**：LLM 返回的 `matchedCharacterId` 必须在候选库内，否则降级
  - **降级原则**：任何失败（网络/超时/解析/白名单失败）都走 `TemplateReportGeneratorAdapter`。**付费必得报告**。
- 严禁单元测试调真实 LLM（flaky + 烧钱），全用 Mock AiWorker。

### D3. Session 状态机（手写 enum + guard）

```
CREATED → PAID → ASKING → ANSWERED_ALL → REPORT_READY → EXPIRED
```

- 付费前置：CREATED 后第一件事付费，PAID 才进 ASKING。
- `domain/session/SessionTransition` 集中所有 guard 方法，不引入 Spring StateMachine 框架。
- 转换在 NodeService 内调 `SessionTransition.markXxx(s)`，AppService 不直接碰状态细节。

### D4. MVP 持久化策略

- Session / Payment 内存仓储（`InMemorySessionRepository` / `InMemoryPaymentRepository` + `ConcurrentHashMap`）。
- 候选人物 + 题库从 `src/main/resources/data/*.json` 启动加载到内存（`JsonResourceCharacterAdapter` / `JsonResourceQuestionAdapter`）。
- 上 JPA 是过度设计。换 DB 时只新写仓储 adapter 实现 `SessionRepositoryPort` 等端口即可。

### D5. 支付幂等

- `PaymentVerifyNode.charge` 先查 `findSuccessBySessionId`：同 session 已有成功支付直接返回（idempotentHit=true），不再向网关发起。
- 换真实网关时建议加 DB 唯一索引保证幂等性。

## 命名规范

- 类：`UpperCamelCase`。
- 包：全小写、单数。
- 端口接口：`*Port`（南向）、领域内策略端口不加 Port 后缀（`InferenceEngine`、`NextQuestionPolicy`）。
- Adapter 实现：`*Adapter`（南向具体类，如 `JsonResourceCharacterAdapter`）。
- AppService：`*AppService`。Node：`*Node`。Controller：`*Controller`。ACL：`*Acl`。
- 常量 / 枚举值：`UPPER_SNAKE_CASE`。
- JSON 数据文件：`lower-kebab-case.json`，放 `src/main/resources/data/`。

## 红线（即使 auto-accept 也必须先问我）

- 修改 `application.properties` 里的 `DEEPSEEK_API_KEY` —— 必须走环境变量，不进文件、不进 commit
- 删除已写好的推理纯函数或测试
- 接真实支付网关（踩「公开发布到生产」红线，必须你亲自接入）
- 修改本 CLAUDE.md 中的「分层依赖方向」「架构决策」两节 —— 改规范前先问我
- 跨分层反向依赖（如 domain 调 north、south 不经 port 直连业务）

## 验证命令

- `mvn compile` —— 编译必须通过
- `mvn test` —— 全部单测必须通过。重点看 `ScorerTest`、`InformationGainPolicyTest`、`LlmReportGeneratorAdapterTest`、`PaymentVerifyNodeTest`。
- `mvn spring-boot:run` —— 启动应用做端到端冒烟（需设环境变量 `DEEPSEEK_API_KEY`）。
- **严禁测 LLM 真实输出**（flaky + 烧钱），全部用 Mock AiWorker。

## 工程纪律

- 改完主动跑 `mvn test`，不要只改不验。
- 不要为了让代码跑起来注释报错或加绕过标记，找根本原因。
- 密钥、token、密码不进代码、不进 commit、不进日志。
- 大改动前先出 Plan，确认后再动手。
- 不为用模式而用模式：策略/端口/工厂只在真有扩展价值处用。零散一两行的支线不抽接口。