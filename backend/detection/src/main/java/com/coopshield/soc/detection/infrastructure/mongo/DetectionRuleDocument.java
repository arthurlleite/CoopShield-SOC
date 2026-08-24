package com.coopshield.soc.detection.infrastructure.mongo;

import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Modelo de persistencia da colecao {@code detection_rules} - espelho
 * consultavel das regras carregadas de YAML (ver
 * {@code DetectionRuleRepository}). A fonte da verdade permanece o YAML.
 */
@Document(collection = "detection_rules")
public class DetectionRuleDocument {

    @Id
    private String id;

    private String name;
    private String description;
    private boolean enabled;
    private List<String> eventTypes;
    private String evaluatorType;
    private Map<String, String> conditions;
    private String aggregationWindow;
    private int threshold;
    private Severity severity;
    private int baseRiskScore;
    private String mitreTactic;
    private String mitreTechnique;
    private String recommendedPlaybook;
    private String version;
    private String falsePositiveNotes;
    private List<String> references;
    private String author;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    protected DetectionRuleDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public DetectionRuleDocument(String id, String name, String description, boolean enabled, List<String> eventTypes,
                                  String evaluatorType, Map<String, String> conditions, String aggregationWindow,
                                  int threshold, Severity severity, int baseRiskScore, String mitreTactic,
                                  String mitreTechnique, String recommendedPlaybook, String version,
                                  String falsePositiveNotes, List<String> references, String author,
                                  LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.eventTypes = eventTypes;
        this.evaluatorType = evaluatorType;
        this.conditions = conditions;
        this.aggregationWindow = aggregationWindow;
        this.threshold = threshold;
        this.severity = severity;
        this.baseRiskScore = baseRiskScore;
        this.mitreTactic = mitreTactic;
        this.mitreTechnique = mitreTechnique;
        this.recommendedPlaybook = recommendedPlaybook;
        this.version = version;
        this.falsePositiveNotes = falsePositiveNotes;
        this.references = references;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DetectionRuleDocument fromDomain(DetectionRule rule) {
        return new DetectionRuleDocument(
                rule.id(), rule.name(), rule.description(), rule.enabled(), rule.eventTypes(), rule.evaluatorType(),
                rule.conditions(), rule.aggregationWindow().toString(), rule.threshold(), rule.severity(),
                rule.baseRiskScore(), rule.mitreTactic(), rule.mitreTechnique(), rule.recommendedPlaybook(),
                rule.version(), rule.falsePositiveNotes(), rule.references(), rule.author(), rule.createdAt(),
                rule.updatedAt());
    }

    public DetectionRule toDomain() {
        return new DetectionRule(
                id, name, description, enabled, eventTypes, evaluatorType, conditions, Duration.parse(aggregationWindow),
                threshold, severity, baseRiskScore, mitreTactic, mitreTechnique, recommendedPlaybook, version,
                falsePositiveNotes, references, author, createdAt, updatedAt);
    }
}
