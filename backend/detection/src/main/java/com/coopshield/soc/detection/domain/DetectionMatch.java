package com.coopshield.soc.detection.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado explicavel de uma regra acionada (ver
 * docs/detection-rules/catalog.md secao 4 "Explicabilidade Obrigatoria").
 * Carrega tudo que um alerta (Fase 8) e o motor de risco (Fase 7)
 * precisarao: qual regra, quais eventos evidenciaram, qual limite foi
 * ultrapassado e por qual valor observado, e o mapeamento MITRE.
 *
 * <p>{@code riskScore} usa {@code baseRiskScore} da regra como valor
 * provisorio - o calculo explicavel combinando severidade, dispositivo,
 * volume e reincidencia e responsabilidade do motor de risco (Fase 7, ainda
 * nao implementado), mesmo padrao provisorio de
 * {@code EventNormalizationService}/{@code DataClassification} na Fase 4.
 */
public record DetectionMatch(
        UUID matchId,
        String ruleId,
        String ruleName,
        String actorUserId,
        int observedValue,
        int threshold,
        List<String> evidenceEventIds,
        Severity severity,
        int riskScore,
        String mitreTactic,
        String mitreTechnique,
        String recommendedPlaybook,
        String correlationId,
        Instant detectedAt
) {

    public DetectionMatch {
        Objects.requireNonNull(matchId, "matchId must not be null");
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(actorUserId, "actorUserId must not be null");
        Objects.requireNonNull(evidenceEventIds, "evidenceEventIds must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        Objects.requireNonNull(detectedAt, "detectedAt must not be null");
        if (evidenceEventIds.isEmpty()) {
            throw new IllegalArgumentException("evidenceEventIds must not be empty");
        }
        evidenceEventIds = List.copyOf(evidenceEventIds);
    }
}
