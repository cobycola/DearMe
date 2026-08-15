package com.zionysus.dearme.south.port;

import com.zionysus.dearme.domain.question.Question;

import java.util.List;
import java.util.Map;

/**
 * 题库来源端口（南向 outbound port）。
 *
 * 扩展点：
 *   - 静态 JSON 加载（当前）
 *   - RAG 知识库检索（未来）
 *   - LLM 兜底现场变形（未来）
 *   - DB 后台维护（未来）
 */
public interface QuestionSourcePort {

    List<String> supportedTopics();

    List<Question> questions(String topicId);

    Map<String, Question> questionIndex(String topicId);
}