package com.zionysus.dearme.south.adapter.persistence;

import com.zionysus.dearme.domain.inference.TraitDimension;
import com.zionysus.dearme.domain.question.Question;
import com.zionysus.dearme.south.port.QuestionSourcePort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 从 classpath JSON 加载题库的 adapter（南向）。
 * MVP 单主题：所有 topicId 映射同一份 questions.json。
 *
 * 扩展点：未来按 topicId 路由不同 JSON / DB / RAG / LLM 现场变形。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonResourceQuestionAdapter implements QuestionSourcePort {

    private final ObjectMapper objectMapper;

    @Value("${dearme.data.questions-path:data/questions.json}")
    private String questionsPath;

    private List<Question> questions;
    private Map<String, Question> questionIndex;

    @PostConstruct
    public void init() {
        try (InputStream in = new ClassPathResource(questionsPath).getInputStream()) {
            List<RawQuestion> raws = objectMapper.readValue(in,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RawQuestion.class));
            this.questions = raws.stream()
                    .map(JsonResourceQuestionAdapter::toQuestion)
                    .toList();
            this.questionIndex = questions.stream()
                    .collect(Collectors.toUnmodifiableMap(Question::getId, Function.identity()));
            log.info("JsonResourceQuestionAdapter 加载：题库 {} 题", questions.size());
        } catch (Exception e) {
            throw new IllegalStateException("加载题库失败: " + questionsPath, e);
        }
    }

    private static Question toQuestion(RawQuestion r) {
        return Question.builder()
                .id(r.getId())
                .prompt(r.getPrompt())
                .dimension(TraitDimension.valueOf(r.getDimension()))
                .options(List.of(r.getOptions()))
                .fewerInfoOptionIndex(r.getFewerInfoOptionIndex())
                .build();
    }

    @Override
    public List<String> supportedTopics() {
        return List.of("anime-character");
    }

    @Override
    public List<Question> questions(String topicId) {
        if (!supportedTopics().contains(topicId)) {
            throw new IllegalArgumentException("不支持的 topicId: " + topicId);
        }
        return questions;
    }

    @Override
    public Map<String, Question> questionIndex(String topicId) {
        if (!supportedTopics().contains(topicId)) {
            throw new IllegalArgumentException("不支持的 topicId: " + topicId);
        }
        return questionIndex;
    }

    /** Jackson 3 反序列化用 POJO（与 JSON 字段一一对应）。 */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RawQuestion {
        private String id;
        private String prompt;
        private String dimension;
        private String[] options;
        private int fewerInfoOptionIndex;
    }
}