package com.zionysus.dearme.south.adapter.persistence.jdbc;

import com.zionysus.dearme.domain.question.Answer;
import com.zionysus.dearme.domain.session.Session;
import com.zionysus.dearme.domain.session.SessionStatus;
import com.zionysus.dearme.south.port.SessionRepositoryPort;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcSessionRepository 集成测试：进程内 embedded-postgres 验证 jsonb 整列存聚合根的可往返性。
 * 不启 Spring context，直接 wired JdbcTemplate + ObjectMapper，隔离干净、起停快。
 */
class JdbcSessionRepositoryTest {

    private static EmbeddedPostgres pg;
    private static SessionRepositoryPort repo;

    @BeforeAll
    static void start() throws Exception {
        pg = EmbeddedPostgres.builder().start();
        DataSource ds = pg.getPostgresDatabase();
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(ds);
        repo = new JdbcSessionRepository(new JdbcTemplate(ds), new ObjectMapper());
    }

    @AfterAll
    static void stop() throws Exception {
        if (pg != null) {
            pg.close();
        }
    }

    @Test
    void saveAndFindByIdRoundtripsAggregate() {
        // 用 reconstitute 构造一个有内容的"历史态"聚合根，直接验证 jsonb 往返、不经状态机迁移
        Answer answer = Answer.builder().questionId("q1").optionIndex(2).build();
        Session s = Session.reconstitute(
                "sess-roundtrip", "anime-character", Instant.now(),
                SessionStatus.ASKING, List.of(answer),
                "# 报告\n你像鸣人", null);
        repo.save(s);

        Session loaded = repo.findById("sess-roundtrip").orElseThrow();
        assertThat(loaded.getTopicId()).isEqualTo("anime-character");
        assertThat(loaded.getStatus()).isEqualTo(SessionStatus.ASKING);
        assertThat(loaded.getAnswers()).hasSize(1);
        assertThat(loaded.getAnswers().get(0).getQuestionId()).isEqualTo("q1");
        assertThat(loaded.getReportContent()).isEqualTo("# 报告\n你像鸣人");
        assertThat(loaded.getExpiredAt()).isNull();
        assertThat(loaded.getCreatedAt()).isEqualTo(s.getCreatedAt());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(repo.findById("does-not-exist")).isEmpty();
    }

    @Test
    void saveIsIdempotentUpsertOverwritesAnswers() {
        Session s = Session.reconstitute(
                "sess-upsert", "anime-character", Instant.now(),
                SessionStatus.ASKING, List.of(),
                null, null);
        repo.save(s);
        // 覆盖成有 answer 的形态（同 id）
        Answer answer = Answer.builder().questionId("q2").optionIndex(1).build();
        Session updated = Session.reconstitute(
                "sess-upsert", "anime-character", s.getCreatedAt(),
                SessionStatus.ANSWERED_ALL, List.of(answer),
                "更新报告", null);
        repo.save(updated);

        Session loaded = repo.findById("sess-upsert").orElseThrow();
        assertThat(loaded.getAnswers()).hasSize(1);
        assertThat(loaded.getAnswers().get(0).getQuestionId()).isEqualTo("q2");
        assertThat(loaded.getStatus()).isEqualTo(SessionStatus.ANSWERED_ALL);
        assertThat(loaded.getReportContent()).isEqualTo("更新报告");
    }
}