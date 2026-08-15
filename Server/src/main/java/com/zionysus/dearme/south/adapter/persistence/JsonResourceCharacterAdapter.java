package com.zionysus.dearme.south.adapter.persistence;

import com.zionysus.dearme.domain.inference.CharacterProfile;
import com.zionysus.dearme.south.port.CharacterSourcePort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

/**
 * 从 classpath JSON 加载候选人物的 adapter（南向）。
 *
 * MVP 单主题：所有 topicId 映射到同一份 characters.json。
 * 扩展点：未来按 topicId 路由不同 JSON / DB / RAG。
 *
 * Jackson 3 注入：Spring Boot 4 自动配置 ObjectMapper bean（tools.jackson.databind.ObjectMapper），
 * 不再 new。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonResourceCharacterAdapter implements CharacterSourcePort {

    private final ObjectMapper objectMapper;

    @Value("${dearme.data.characters-path:data/characters.json}")
    private String charactersPath;

    private List<CharacterProfile> characters;

    @PostConstruct
    public void init() {
        try (InputStream in = new ClassPathResource(charactersPath).getInputStream()) {
            this.characters = List.copyOf(objectMapper.readValue(in, objectMapper.getTypeFactory().constructCollectionType(List.class, CharacterProfile.class)));
            log.info("JsonResourceCharacterAdapter 加载：候选 {} 个", characters.size());
        } catch (Exception e) {
            throw new IllegalStateException("加载候选库失败: " + charactersPath, e);
        }
    }

    @Override
    public List<String> supportedTopics() {
        return List.of("anime-character");
    }

    @Override
    public List<CharacterProfile> characters(String topicId) {
        if (!supportedTopics().contains(topicId)) {
            throw new IllegalArgumentException("不支持的 topicId: " + topicId);
        }
        return characters;
    }
}