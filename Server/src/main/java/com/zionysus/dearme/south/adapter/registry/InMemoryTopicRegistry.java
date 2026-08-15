package com.zionysus.dearme.south.adapter.registry;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.domain.inference.Topic;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.south.port.CharacterSourcePort;
import com.zionysus.dearme.south.port.QuestionSourcePort;
import com.zionysus.dearme.south.port.TopicRegistryPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主题注册表内存实现（南向）。
 *
 * MVP 单主题硬编码注册 anime-character。
 * 多主题扩展点：从配置/DB 加载 Topic 列表，按 sourceKey 路由不同 source。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryTopicRegistry implements TopicRegistryPort {

    private final CharacterSourcePort characterSource;
    private final QuestionSourcePort questionSource;

    private final Map<String, Topic> topics = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        register(new Topic(
                "anime-character",
                "看看我像哪个动漫人物",
                "通过逐题定制问卷，推理你最像的动漫角色",
                "json",
                "json",
                "weighted-trait"
        ));
        log.info("InMemoryTopicRegistry 注册主题 {} 个：{}", topics.size(), topics.keySet());
    }

    @Override
    public void register(Topic topic) {
        topics.put(topic.getId(), topic);
    }

    @Override
    public Optional<Topic> topic(String topicId) {
        return Optional.ofNullable(topics.get(topicId));
    }

    @Override
    public List<Topic> all() {
        return List.copyOf(topics.values());
    }

    @Override
    public List<CharacterProfile> characters(String topicId) {
        requireTopic(topicId);
        return characterSource.characters(topicId);
    }

    @Override
    public List<Question> questions(String topicId) {
        requireTopic(topicId);
        return questionSource.questions(topicId);
    }

    @Override
    public Map<String, Question> questionIndex(String topicId) {
        requireTopic(topicId);
        return questionSource.questionIndex(topicId);
    }

    private void requireTopic(String topicId) {
        if (!topics.containsKey(topicId)) {
            throw new IllegalArgumentException("未知主题: " + topicId);
        }
    }
}