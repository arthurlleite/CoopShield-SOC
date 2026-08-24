package com.coopshield.soc.detection.domain;

import com.coopshield.soc.sharedkernel.event.Actor;
import com.coopshield.soc.sharedkernel.event.DataClassification;
import com.coopshield.soc.sharedkernel.event.DeviceContext;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.event.NetworkContext;
import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.event.Target;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;

import java.time.Instant;
import java.util.Map;

/**
 * Fabrica de {@link EventEnvelope} sintetico para os testes deste modulo,
 * evitando repetir os catorze argumentos do record em cada cenario. Publica
 * (nao package-private) para ser reutilizada pelos testes de
 * {@code application} e {@code infrastructure}.
 */
public final class TestEvents {

    private TestEvents() {
    }

    public static Builder builder(String eventType) {
        return new Builder(eventType);
    }

    public static final class Builder {
        private final String eventType;
        private Instant timestamp = Instant.now();
        private String actorUserId = "synthetic-user-01";
        private String actorRole = "EMPLOYEE";
        private String targetResourceType = "account";
        private String targetResourceId = "synthetic-account-01";
        private String action = "ACTION";
        private Outcome outcome = Outcome.SUCCESS;
        private String deviceId = "synthetic-device-01";
        private boolean deviceKnown = true;
        private String geo = "synthetic-region";
        private DataClassification dataClassification = DataClassification.INTERNAL;
        private CorrelationId correlationId = CorrelationId.newId();
        private Map<String, String> metadata = Map.of();

        private Builder(String eventType) {
            this.eventType = eventType;
        }

        public Builder timestamp(Instant value) {
            this.timestamp = value;
            return this;
        }

        public Builder actor(String userId, String role) {
            this.actorUserId = userId;
            this.actorRole = role;
            return this;
        }

        public Builder device(String deviceId, boolean known) {
            this.deviceId = deviceId;
            this.deviceKnown = known;
            return this;
        }

        public Builder geo(String value) {
            this.geo = value;
            return this;
        }

        public Builder outcome(Outcome value) {
            this.outcome = value;
            return this;
        }

        public Builder correlationId(CorrelationId value) {
            this.correlationId = value;
            return this;
        }

        public Builder metadata(Map<String, String> value) {
            this.metadata = value;
            return this;
        }

        public EventEnvelope build() {
            return new EventEnvelope(
                    EventId.newId(), "1.0", eventType, timestamp, "test",
                    new Actor(actorUserId, actorRole, "synthetic-unit"),
                    new Target(targetResourceType, targetResourceId),
                    action, outcome,
                    new DeviceContext(deviceId, deviceKnown),
                    new NetworkContext("hashed-ip", geo),
                    dataClassification, correlationId, metadata);
        }
    }
}
