package com.zionysus.dearme.domain.inference.policy;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.question.Question;

import java.util.List;
import java.util.Map;

/**
 * 下一题策略端口（领域内策略）。
 *
 * 当前实现：InformationGainPolicy —— 信息增益 + 同维度惩罚。
 * 未来扩展点（agent 知识库定制方向）：
 *   - 策展顺序选题（运营人工排题序）
 *   - 随机扰动选题（打破贪心）
 *   - LLM 兜底选题（题库区分度不够时现场变形）
 *   - RAG 选题（从主题向量库检索相关题补充）
 */
public interface NextQuestionPolicy {

    /** 选下一题；返回 null 表示题已答完应进入报告阶段。 */
    Question select(List<Question> allQuestions,
                    List<Answer> answered,
                    List<CharacterProfile> candidates,
                    Map<String, Question> questionById);
}