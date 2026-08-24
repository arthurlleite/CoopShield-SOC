package com.coopshield.soc.detection.infrastructure.web;

import com.coopshield.soc.detection.domain.DetectionRule;

import java.util.List;

public record DetectionRuleResponse(
        String id, String name, String description, boolean enabled, List<String> eventTypes,
        String severity, int baseRiskScore, String mitreTactic, String mitreTechnique,
        String recommendedPlaybook, String version
) {

    public static DetectionRuleResponse from(DetectionRule rule) {
        return new DetectionRuleResponse(
                rule.id(), rule.name(), rule.description(), rule.enabled(), rule.eventTypes(),
                rule.severity().name(), rule.baseRiskScore(), rule.mitreTactic(), rule.mitreTechnique(),
                rule.recommendedPlaybook(), rule.version());
    }
}
