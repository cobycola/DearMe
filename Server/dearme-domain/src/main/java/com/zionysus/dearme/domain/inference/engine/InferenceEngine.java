package com.zionysus.dearme.domain.inference.engine;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.domain.report.InferenceSummary;

import java.util.List;
import java.util.Map;

/**
 * 推理引擎端口（领域内策略）。
 *
 * 当前实现：WeightedTraitEngine —— 加权特征 + 高斯核。
 * 未来扩展点：
 *   - 贝叶斯后验更新引擎
 *   - 纯 LLM 链式推理引擎
 *   - 混合两阶段引擎
 *
 * 注：领域内策略端口的物理位置在 domain，而非 south/port —— 因为推理是
 * 核心领域逻辑，不是「外部依赖」。AI 调用才在 south/port，那里的实现在 adapter。
 */
public interface InferenceEngine {

    InferenceSummary infer(List<Answer> answers,
                            List<CharacterProfile> candidates,
                            Map<String, Question> questionById);
}