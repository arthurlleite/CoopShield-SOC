package com.coopshield.soc.detection.infrastructure.web;

import com.coopshield.soc.detection.domain.DetectionMatch;

import java.time.Instant;
import java.util.List;

public record DetectionMatchResponse(
        String matchId, String ruleId, String ruleName, String actorUserId, int observedValue, int threshold,
        List<String> evidenceEventIds, String severity, int riskScore, String mitreTactic, String mitreTechnique,
        String recommendedPlaybook, String correlationId, Instant detectedAt
) {

    public static DetectionMatchResponse from(DetectionMatch match) {
        return new DetectionMatchResponse(
                match.matchId().toString(), match.ruleId(), match.ruleName(), match.actorUserId(),
                match.observedValue(), match.threshold(), match.evidenceEventIds(), match.severity().name(),
                match.riskScore(), match.mitreTactic(), match.mitreTechnique(), match.recommendedPlaybook(),
                match.correlationId(), match.detectedAt());
    }
}
