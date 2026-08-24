package com.coopshield.soc.audit.infrastructure.mongo;

import com.coopshield.soc.audit.domain.AuditEvent;
import com.coopshield.soc.audit.domain.AuditEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

/**
 * Testes de persistencia real contra um MongoDB efemero (Testcontainers)
 * para {@link MongoAuditLog} - ver docs/adr/ADR-012-mongodb-real-fase-3.md.
 */
@DataMongoTest
@Testcontainers
class MongoAuditLogTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    @Autowired
    private SpringDataAuditEventMongoRepository springDataRepository;

    @Test
    void recordsAndReturnsEventsPersisted() {
        MongoAuditLog log = new MongoAuditLog(springDataRepository);
        AuditEvent first = AuditEvent.of(AuditEventType.AUTHENTICATION_SUCCESS, "synthetic-analyst-01", Map.of());
        AuditEvent second = AuditEvent.of(AuditEventType.LOGOUT, "synthetic-analyst-01", Map.of("reason", "user-initiated"));

        log.record(first);
        log.record(second);

        // MongoDB (BSON Date) armazena timestamps com precisao de
        // milissegundos; comparar por igualdade estrita do record falharia
        // por causa da precisao de nanossegundos perdida no round-trip, sem
        // que isso represente um problema real de persistencia.
        assertThat(log.findAll())
                .extracting(AuditEvent::eventId, AuditEvent::eventType, AuditEvent::actor, AuditEvent::details)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(first.eventId(), first.eventType(), first.actor(), first.details()),
                        org.assertj.core.groups.Tuple.tuple(second.eventId(), second.eventType(), second.actor(), second.details()));
        assertThat(log.findAll())
                .allSatisfy(event -> assertThat(event.timestamp())
                        .isCloseTo(first.timestamp(), byLessThan(1, ChronoUnit.SECONDS)));
    }

    @Test
    void preservesDetailsMap() {
        MongoAuditLog log = new MongoAuditLog(springDataRepository);
        AuditEvent event = AuditEvent.of(AuditEventType.AUTHORIZATION_DENIED, "synthetic-employee-01",
                Map.of("path", "/api/v1/admin/example"));

        log.record(event);

        AuditEvent reloaded = log.findAll().stream()
                .filter(e -> e.eventId().equals(event.eventId()))
                .findFirst()
                .orElseThrow();
        assertThat(reloaded.details()).containsEntry("path", "/api/v1/admin/example");
    }
}
