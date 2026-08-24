package com.coopshield.soc.eventnormalization.application;

import com.coopshield.soc.sharedkernel.event.EventEnvelope;

/**
 * Porta de saida para publicacao de eventos normalizados no topico
 * {@code security.normalized-events}.
 */
public interface NormalizedEventPublisher {

    void publish(EventEnvelope event);
}
