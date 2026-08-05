package com.coopshield.soc.audit.infrastructure;

import com.coopshield.soc.audit.domain.AuditEvent;
import com.coopshield.soc.audit.domain.AuditEventType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryAuditLogTest {

    @Test
    void recordsAndReturnsEventsInOrder() {
        InMemoryAuditLog log = new InMemoryAuditLog();
        AuditEvent first = AuditEvent.of(AuditEventType.AUTHENTICATION_SUCCESS, "synthetic-analyst-01", Map.of());
        AuditEvent second = AuditEvent.of(AuditEventType.LOGOUT, "synthetic-analyst-01", Map.of());

        log.record(first);
        log.record(second);

        assertThat(log.findAll()).containsExactly(first, second);
    }

    @Test
    void findAllResultIsUnmodifiable() {
        InMemoryAuditLog log = new InMemoryAuditLog();

        assertThatThrownBy(() -> log.findAll().add(
                AuditEvent.of(AuditEventType.LOGOUT, "synthetic-analyst-01", Map.of())))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
