package com.coopshield.soc.sharedkernel.event;

import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventEnvelopeTest {

    @Test
    void createsValidEnvelope() {
        EventEnvelope envelope = anEnvelope().build();

        assertThat(envelope.eventType()).isEqualTo("authentication.login.failure");
        assertThat(envelope.outcome()).isEqualTo(Outcome.FAILURE);
        assertThat(envelope.dataClassification()).isEqualTo(DataClassification.SENSITIVE);
    }

    @Test
    void metadataIsImmutable() {
        Map<String, String> mutableMetadata = new HashMap<>();
        mutableMetadata.put("key", "value");

        EventEnvelope envelope = anEnvelope().metadata(mutableMetadata).build();
        mutableMetadata.put("another", "value");

        assertThat(envelope.metadata()).containsOnly(Map.entry("key", "value"));
        assertThatThrownBy(() -> envelope.metadata().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsBlankEventType() {
        assertThatThrownBy(() -> anEnvelope().eventType(" ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventType");
    }

    @Test
    void rejectsNullActor() {
        assertThatThrownBy(() -> anEnvelope().actor(null).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("actor");
    }

    private TestEnvelopeBuilder anEnvelope() {
        return new TestEnvelopeBuilder();
    }

    /**
     * Pequeno builder de teste para manter os cenarios acima legiveis sem
     * repetir os catorze argumentos do record em cada teste.
     */
    private static final class TestEnvelopeBuilder {
        private EventId eventId = EventId.newId();
        private String eventVersion = "1.0";
        private String eventType = "authentication.login.failure";
        private Instant timestamp = Instant.parse("2026-08-05T14:32:10Z");
        private String source = "identity-service";
        private Actor actor = new Actor("synthetic-user-001", "EMPLOYEE", "synthetic-branch-001");
        private Target target = new Target("account", "synthetic-account-001");
        private String action = "LOGIN";
        private Outcome outcome = Outcome.FAILURE;
        private DeviceContext device = new DeviceContext("synthetic-device-001", false);
        private NetworkContext networkContext = new NetworkContext("hashed-ip", "synthetic-region");
        private DataClassification dataClassification = DataClassification.SENSITIVE;
        private CorrelationId correlationId = CorrelationId.newId();
        private Map<String, String> metadata = Map.of();

        TestEnvelopeBuilder eventType(String value) {
            this.eventType = value;
            return this;
        }

        TestEnvelopeBuilder actor(Actor value) {
            this.actor = value;
            return this;
        }

        TestEnvelopeBuilder metadata(Map<String, String> value) {
            this.metadata = value;
            return this;
        }

        EventEnvelope build() {
            return new EventEnvelope(eventId, eventVersion, eventType, timestamp, source, actor,
                    target, action, outcome, device, networkContext, dataClassification,
                    correlationId, metadata);
        }
    }
}
