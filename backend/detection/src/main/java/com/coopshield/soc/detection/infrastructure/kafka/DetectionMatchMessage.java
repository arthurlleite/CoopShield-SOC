package com.coopshield.soc.detection.infrastructure.kafka;

import com.coopshield.soc.detection.domain.DetectionMatch;

import java.time.Instant;
import java.util.List;

/**
 * Forma de serializacao (JSON) de {@link DetectionMatch}, publicada em
 * {@code security.detection-alerts}.
 */
public record DetectionMatchMessage(
        String matchId,
        String ruleId,
        String ruleName,
        String actorUserId,
        int observedValue,
        int threshold,
        List<String> evidenceEventIds,
        String severity,
        int riskScore,
        String mitreTactic,
        String mitreTechnique,
        String recommendedPlaybook,
        String correlationId,
        Instant detectedAt
) {

    public static DetectionMatchMessage from(DetectionMatch match) {
        return new DetectionMatchMessage(
                match.matchId().toString(), match.ruleId(), match.ruleName(), match.actorUserId(),
                match.observedValue(), match.threshold(), match.evidenceEventIds(), match.severity().name(),
                match.riskScore(), match.mitreTactic(), match.mitreTechnique(), match.recommendedPlaybook(),
                match.correlationId(), match.detectedAt());
    }
}
