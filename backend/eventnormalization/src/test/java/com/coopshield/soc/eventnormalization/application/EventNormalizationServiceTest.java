package com.coopshield.soc.eventnormalization.application;

import com.coopshield.soc.eventnormalization.domain.EventNormalizationException;
import com.coopshield.soc.sharedkernel.event.DataClassification;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventNormalizationServiceTest {

    @Mock
    private NormalizedEventRepository repository;
    @Mock
    private NormalizedEventPublisher publisher;

    private EventNormalizationService service;

    @BeforeEach
    void setUp() {
        service = new EventNormalizationService(repository, publisher);
    }

    @Test
    void hashesSourceIpAssignsProvisionalClassificationAndPublishesWhenNewlyPersisted() {
        when(repository.saveIfAbsent(any())).thenReturn(true);

        service.normalize(
                UUID.randomUUID().toString(), "1.0", "authentication.login.failure",
                Instant.parse("2026-08-24T12:00:00Z"), "identity-service",
                "synthetic-user-01", "EMPLOYEE", "synthetic-branch-001", "account", "synthetic-account-01",
                "LOGIN", "FAILURE", "synthetic-device-01", false, "203.0.113.10", "synthetic-region",
                UUID.randomUUID().toString(), Map.of());

        ArgumentCaptor<EventEnvelope> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(repository).saveIfAbsent(captor.capture());
        EventEnvelope envelope = captor.getValue();

        assertThat(envelope.networkContext().ipHash()).isNotEqualTo("203.0.113.10");
        assertThat(envelope.networkContext().ipHash()).isEqualTo(IpHasher.hash("203.0.113.10"));
        assertThat(envelope.dataClassification()).isEqualTo(DataClassification.INTERNAL);
        assertThat(envelope.outcome()).isEqualTo(Outcome.FAILURE);

        verify(publisher).publish(envelope);
    }

    @Test
    void doesNotRepublishWhenEventWasAlreadyPersisted() {
        when(repository.saveIfAbsent(any())).thenReturn(false);

        service.normalize(
                UUID.randomUUID().toString(), "1.0", "authentication.login.failure",
                Instant.parse("2026-08-24T12:00:00Z"), "identity-service",
                "synthetic-user-01", "EMPLOYEE", null, "account", "synthetic-account-01",
                "LOGIN", "FAILURE", "synthetic-device-01", false, "203.0.113.10", null,
                UUID.randomUUID().toString(), null);

        verify(publisher, never()).publish(any());
    }

    @Test
    void rejectsMissingRequiredFieldsWithoutPersistingOrPublishing() {
        assertThatThrownBy(() -> service.normalize(
                null, "1.0", "", null, "",
                "", "", null, "", "",
                "", "not-an-outcome", "", false, "", null,
                "not-a-uuid", null))
                .isInstanceOf(EventNormalizationException.class)
                .satisfies(e -> assertThat(((EventNormalizationException) e).violations()).hasSizeGreaterThan(5));

        verify(repository, never()).saveIfAbsent(any());
        verify(publisher, never()).publish(any());
    }
}
