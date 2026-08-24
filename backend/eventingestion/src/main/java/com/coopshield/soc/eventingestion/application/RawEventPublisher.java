package com.coopshield.soc.eventingestion.application;

import com.coopshield.soc.eventingestion.domain.RawEvent;

/**
 * Porta de saida para publicacao de eventos brutos validados no topico
 * {@code security.raw-events}.
 */
public interface RawEventPublisher {

    void publish(RawEvent event);
}
