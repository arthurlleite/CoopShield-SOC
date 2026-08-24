package com.coopshield.soc.detection.infrastructure.yaml;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Forma de deserializacao (YAML) de uma regra de deteccao, espelhando o
 * schema documentado em docs/detection-rules/catalog.md, com o acrescimo do
 * campo {@code evaluatorType} (ver ADR-014) que seleciona qual
 * {@code RuleEvaluator} interpreta {@code conditions}.
 */
public record DetectionRuleYamlDocument(
        String id,
        String name,
        String description,
        boolean enabled,
        List<String> eventTypes,
        String evaluatorType,
        Map<String, String> conditions,
        Duration aggregationWindow,
        int threshold,
        String severity,
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
}
