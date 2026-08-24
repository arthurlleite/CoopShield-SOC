package com.coopshield.soc.detection.infrastructure.kafka;

/**
 * Nomes dos topicos Kafka relevantes para {@code detection}, conforme
 * docs/event-catalog/events.md. {@code NORMALIZED_EVENTS} e duplicado (nao
 * compartilhado via dependencia de modulo) do equivalente em
 * {@code eventnormalization} deliberadamente - ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
public final class DetectionTopics {

    public static final String NORMALIZED_EVENTS = "security.normalized-events";
    public static final String DETECTION_ALERTS = "security.detection-alerts";

    private DetectionTopics() {
    }
}
