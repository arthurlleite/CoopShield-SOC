package com.coopshield.soc.eventnormalization.infrastructure.kafka;

/**
 * Nomes dos topicos Kafka relevantes para {@code eventnormalization},
 * conforme docs/event-catalog/events.md. {@code RAW_EVENTS} e duplicado
 * (nao compartilhado via dependencia de modulo) do equivalente em
 * {@code eventingestion} deliberadamente: o contrato entre os dois modulos
 * e o topico Kafka em si, nao um tipo Java compartilhado - ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
public final class EventTopics {

    public static final String RAW_EVENTS = "security.raw-events";
    public static final String NORMALIZED_EVENTS = "security.normalized-events";
    public static final String DEAD_LETTER = "security.dead-letter";

    private EventTopics() {
    }
}
