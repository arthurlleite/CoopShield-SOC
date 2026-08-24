package com.coopshield.soc.eventingestion.infrastructure.kafka;

/**
 * Nomes dos topicos Kafka relevantes para {@code eventingestion}, conforme
 * docs/event-catalog/events.md. Duplicado (nao compartilhado via dependencia
 * de modulo) em {@code eventnormalization} deliberadamente: o contrato entre
 * os dois modulos e o topico Kafka em si, nao um tipo Java compartilhado -
 * ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
public final class EventTopics {

    public static final String RAW_EVENTS = "security.raw-events";

    private EventTopics() {
    }
}
