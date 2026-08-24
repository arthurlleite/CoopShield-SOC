package com.coopshield.soc.detection.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Uma regra de deteccao carregada de YAML (ver
 * docs/detection-rules/catalog.md e ADR-006). {@code conditions} carrega os
 * parametros especificos do {@code evaluatorType} desta regra (ex.:
 * {@code httpStatus} para RULE-011/012, {@code role} para RULE-013) - ver
 * ADR-014 para a lista dos sete tipos de avaliador e o que cada um espera
 * encontrar em {@code conditions}.
 */
public record DetectionRule(
        String id,
        String name,
        String description,
        boolean enabled,
        List<String> eventTypes,
        String evaluatorType,
        Map<String, String> conditions,
        Duration aggregationWindow,
        int threshold,
        Severity severity,
        int baseRiskScore,
        String mitreTactic,
        String mitreTechnique,
        String recommendedPlaybook,
        String version,
        String falsePositiveNotes,
        List<String> references,
        String author,
        LocalDate createdAt,
        LocalDate updatedAt
) {

    public DetectionRule {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(eventTypes, "eventTypes must not be null");
        Objects.requireNonNull(evaluatorType, "evaluatorType must not be null");
        Objects.requireNonNull(aggregationWindow, "aggregationWindow must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (eventTypes.isEmpty()) {
            throw new IllegalArgumentException("eventTypes must not be empty");
        }
        eventTypes = List.copyOf(eventTypes);
        conditions = conditions == null ? Map.of() : Map.copyOf(conditions);
        references = references == null ? List.of() : List.copyOf(references);
    }

    public boolean appliesTo(String eventType) {
        return enabled && eventTypes.contains(eventType);
    }
}
