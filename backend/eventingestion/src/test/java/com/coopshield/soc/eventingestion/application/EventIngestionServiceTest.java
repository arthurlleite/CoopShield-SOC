package com.coopshield.soc.eventingestion.application;

import com.coopshield.soc.eventingestion.domain.RawEvent;
import com.coopshield.soc.eventingestion.domain.RawEventValidationException;
import com.coopshield.soc.sharedkernel.event.Outcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private RawEventPublisher publisher;

    private EventIngestionService service;

    @BeforeEach
    void setUp() {
        service = new EventIngestionService(publisher);
    }

    @Test
    void assignsDefaultsAndPublishesWhenOptionalFieldsAreAbsent() {
        RawEvent event = service.ingest(
                null, null, "authentication.login.failure", null, "identity-service",
                "synthetic-user-01", "EMPLOYEE", null, "account", "synthetic-account-01",
                "LOGIN", "failure", "synthetic-device-01", false, "203.0.113.10", null, null, null);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventVersion()).isEqualTo("1.0");
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.correlationId()).isNotNull();
        assertThat(event.outcome()).isEqualTo(Outcome.FAILURE);
        assertThat(event.metadata()).isEmpty();

        ArgumentCaptor<RawEvent> captor = ArgumentCaptor.forClass(RawEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue()).isEqualTo(event);
    }

    @Test
    void preservesProvidedEventIdCorrelationIdAndVersion() {
        String eventId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        Instant timestamp = Instant.parse("2026-08-24T12:00:00Z");

        RawEvent event = service.ingest(
                eventId, "2.0", "authentication.login.success", timestamp, "identity-service",
                "synthetic-user-01", "EMPLOYEE", "synthetic-branch-001", "account", "synthetic-account-01",
                "LOGIN", "SUCCESS", "synthetic-device-01", true, "203.0.113.10", "synthetic-region",
                correlationId, Map.of("k", "v"));

        assertThat(event.eventId().toString()).isEqualTo(eventId);
        assertThat(event.correlationId().toString()).isEqualTo(correlationId);
        assertThat(event.eventVersion()).isEqualTo("2.0");
        assertThat(event.timestamp()).isEqualTo(timestamp);
        assertThat(event.metadata()).containsEntry("k", "v");
    }

    @Test
    void rejectsInvalidOutcomeWithoutPublishing() {
        assertThatThrownBy(() -> service.ingest(
                null, null, "authentication.login.failure", null, "identity-service",
                "synthetic-user-01", "EMPLOYEE", null, "account", "synthetic-account-01",
                "LOGIN", "not-a-real-outcome", "synthetic-device-01", false, "203.0.113.10", null, null, null))
                .isInstanceOf(RawEventValidationException.class)
                .satisfies(e -> assertThat(((RawEventValidationException) e).violations()).hasSize(1));

        verify(publisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsMalformedEventIdAndCorrelationIdTogether() {
        assertThatThrownBy(() -> service.ingest(
                "not-a-uuid", null, "authentication.login.failure", null, "identity-service",
                "synthetic-user-01", "EMPLOYEE", null, "account", "synthetic-account-01",
                "LOGIN", "FAILURE", "synthetic-device-01", false, "203.0.113.10", null, "also-not-a-uuid", null))
                .isInstanceOf(RawEventValidationException.class)
                .satisfies(e -> assertThat(((RawEventValidationException) e).violations()).hasSize(2));

        verify(publisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }
}
