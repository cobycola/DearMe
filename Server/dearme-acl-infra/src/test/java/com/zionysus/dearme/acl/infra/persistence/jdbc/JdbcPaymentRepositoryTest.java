package com.zionysus.dearme.acl.infra.persistence.jdbc;

import com.zionysus.dearme.domain.payment.Payment;
import com.zionysus.dearme.domain.payment.PaymentStatus;
import com.zionysus.dearme.acl.ports.PaymentRepositoryPort;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
class JdbcPaymentRepositoryTest {

    private static EmbeddedPostgres pg;
    private static PaymentRepositoryPort repo;

    @BeforeAll
    static void start() throws Exception {
        pg = EmbeddedPostgres.builder().start();
        DataSource ds = pg.getPostgresDatabase();
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(ds);
        repo = new JdbcPaymentRepository(new JdbcTemplate(ds), new ObjectMapper());
    }

    @AfterAll
    static void stop() throws Exception {
        if (pg != null) {
            pg.close();
        }
    }

    private static Payment successPayment(String sessionId, long amount) {
        return Payment.reconstitute(
                UUID.randomUUID().toString(), sessionId, amount, Instant.now(),
                PaymentStatus.SUCCESS, "ext-" + UUID.randomUUID(), null);
    }

    @Test
    void saveAndFindByIdRoundtripsPayment() {
        Payment p = successPayment("sess-find-id", 1999);
        repo.save(p);

        Payment loaded = repo.findById(p.getId()).orElseThrow();
        assertThat(loaded.getSessionId()).isEqualTo("sess-find-id");
        assertThat(loaded.getAmountCents()).isEqualTo(1999);
        assertThat(loaded.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(loaded.getExternalTxnId()).isEqualTo(p.getExternalTxnId());
        assertThat(loaded.getCreatedAt()).isEqualTo(p.getCreatedAt());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(repo.findById("unknown")).isEmpty();
    }

    @Test
    void findSuccessBySessionIdReturnsOnlySuccessRecord() {
        // 同 session 一笔 FAILED + 一笔 SUCCESS，只回 SUCCESS
        Payment failed = Payment.reconstitute(
                UUID.randomUUID().toString(), "sess-mixed", 100, Instant.now(),
                PaymentStatus.FAILED, null, "余额不足");
        repo.save(failed);

        Payment success = successPayment("sess-mixed", 100);
        repo.save(success);

        Payment found = repo.findSuccessBySessionId("sess-mixed").orElseThrow();
        assertThat(found.getId()).isEqualTo(success.getId());
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void findSuccessBySessionIdReturnsEmptyWhenNoSuccess() {
        Payment pending = Payment.reconstitute(
                UUID.randomUUID().toString(), "sess-none", 100, Instant.now(),
                PaymentStatus.PENDING, null, null);
        repo.save(pending);
        assertThat(repo.findSuccessBySessionId("sess-none")).isEmpty();
    }

    @Test
    void saveOverwritesStateViaUpsert() {
        // PENDING → TIMEOUT 同 id 覆盖
        Payment p = Payment.reconstitute(
                "sess-update-id", "sess-update", 500, Instant.now(),
                PaymentStatus.PENDING, null, null);
        // 用业务构造保证同 id：先 save 一笔自带 id 的，再读回看 status 变化
        repo.save(p);

        Payment updated = Payment.reconstitute(
                "sess-update-id", "sess-update", 500, p.getCreatedAt(),
                PaymentStatus.TIMEOUT, null, "3s 超时");
        repo.save(updated);

        Payment loaded = repo.findById("sess-update-id").orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(PaymentStatus.TIMEOUT);
        assertThat(loaded.getFailureReason()).isEqualTo("3s 超时");
    }
}
