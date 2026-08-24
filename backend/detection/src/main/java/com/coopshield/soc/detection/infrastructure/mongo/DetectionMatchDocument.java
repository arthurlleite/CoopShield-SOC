package com.coopshield.soc.detection.infrastructure.mongo;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de persistencia da colecao {@code detection_matches} - reservada
 * para a Fase 6 (assim como {@code detection_rules}). Cada documento e uma
 * correspondencia explicavel de regra, consumida pelo motor de risco
 * (Fase 7) e pelo modulo de alerta (Fase 8) - ver ADR-014.
 */
@Document(collection = "detection_matches")
public class DetectionMatchDocument {

    @Id
    private String matchId;

    @Indexed
    private String ruleId;

    private String ruleName;

    @Indexed
    private String actorUserId;

    private int observedValue;
    private int threshold;
    private List<String> evidenceEventIds;
    private Severity severity;
    private int riskScore;
    private String mitreTactic;
    private String mitreTechnique;
    private String recommendedPlaybook;

    @Indexed
    private String correlationId;

    private Instant detectedAt;

    protected DetectionMatchDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public DetectionMatchDocument(String matchId, String ruleId, String ruleName, String actorUserId,
                                   int observedValue, int threshold, List<String> evidenceEventIds,
                                   Severity severity, int riskScore, String mitreTactic, String mitreTechnique,
                                   String recommendedPlaybook, String correlationId, Instant detectedAt) {
        this.matchId = matchId;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.actorUserId = actorUserId;
        this.observedValue = observedValue;
        this.threshold = threshold;
        this.evidenceEventIds = evidenceEventIds;
        this.severity = severity;
        this.riskScore = riskScore;
        this.mitreTactic = mitreTactic;
        this.mitreTechnique = mitreTechnique;
        this.recommendedPlaybook = recommendedPlaybook;
        this.correlationId = correlationId;
        this.detectedAt = detectedAt;
    }

    public static DetectionMatchDocument fromDomain(DetectionMatch match) {
        return new DetectionMatchDocument(
                match.matchId().toString(), match.ruleId(), match.ruleName(), match.actorUserId(),
                match.observedValue(), match.threshold(), match.evidenceEventIds(), match.severity(),
                match.riskScore(), match.mitreTactic(), match.mitreTechnique(), match.recommendedPlaybook(),
                match.correlationId(), match.detectedAt());
    }

    public DetectionMatch toDomain() {
        return new DetectionMatch(
                UUID.fromString(matchId), ruleId, ruleName, actorUserId, observedValue, threshold, evidenceEventIds,
                severity, riskScore, mitreTactic, mitreTechnique, recommendedPlaybook, correlationId, detectedAt);
    }
}
