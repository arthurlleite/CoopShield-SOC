package com.coopshield.soc.eventnormalization.application;

import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.identifiers.EventId;

import java.util.Optional;

/**
 * Porta de saida para persistencia idempotente de eventos normalizados
 * (colecao {@code security_events}).
 */
public interface NormalizedEventRepository {

    /**
     * Persiste o evento se {@code eventId} ainda nao existir.
     *
     * @return {@code true} se o evento foi persistido agora (primeira vez);
     *         {@code false} se ja existia (reprocessamento idempotente).
     */
    boolean saveIfAbsent(EventEnvelope event);

    Optional<EventEnvelope> findByEventId(EventId eventId);
}
