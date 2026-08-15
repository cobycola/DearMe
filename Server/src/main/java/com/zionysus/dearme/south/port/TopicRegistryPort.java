package com.zionysus.dearme.south.port;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.Topic;
import com.zionysus.dearme.domain.question.Question;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 主题注册表端口（南向 outbound port）。
 *
 * 聚合所有 CharacterSource / QuestionSource，按 topicId 路由。
 * MVP 单实现 InMemoryTopicRegistry，注册一个 anime-character 主题。
 *
 * 多主题/多 source 扩展点：
 *   - 从配置或 DB 加载 Topic 列表
 *   - 按 Topic.sourceKey 路由到不同 source 实现（多实现共存时 byKey 装配）
 */
public interface TopicRegistryPort {

    void register(Topic topic);

    Optional<Topic> topic(String topicId);

    List<Topic> all();

    List<CharacterProfile> characters(String topicId);

    List<Question> questions(String topicId);

    Map<String, Question> questionIndex(String topicId);
}