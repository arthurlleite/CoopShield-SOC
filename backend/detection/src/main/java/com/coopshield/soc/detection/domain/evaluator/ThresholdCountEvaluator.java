package com.coopshield.soc.detection.domain.evaluator;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RULE-006, RULE-007, RULE-008, RULE-011, RULE-012, RULE-014: conta quantos
 * eventos do(s) {@code eventTypes} da regra (opcionalmente filtrados por um
 * campo de metadata, ver {@code conditions.metadataKey}/{@code metadataEquals}
 * - usado por RULE-011/012 para distinguir 401 de 403) o ator produziu na
 * janela de agregacao, incluindo o evento atual. Aciona quando a contagem
 * atinge o limite.
 */
public class ThresholdCountEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "threshold-count";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        String metadataKey = rule.conditions().get("metadataKey");
        String metadataEquals = rule.conditions().get("metadataEquals");

        List<EventEnvelope> matching = new ArrayList<>();
        for (EventEnvelope candidate : recentHistory) {
            if (matchesRule(candidate, rule, metadataKey, metadataEquals)) {
                matching.add(candidate);
            }
        }
        if (matchesRule(event, rule, metadataKey, metadataEquals)) {
            matching.add(event);
        }

        if (matching.size() < rule.threshold()) {
            return Optional.empty();
        }

        List<String> evidence = matching.stream().map(e -> e.eventId().toString()).toList();
        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                matching.size(), rule.threshold(), evidence, rule.severity(), rule.baseRiskScore(),
                rule.mitreTactic(), rule.mitreTechnique(), rule.recommendedPlaybook(),
                event.correlationId().toString(), Instant.now()));
    }

    private boolean matchesRule(EventEnvelope candidate, DetectionRule rule, String metadataKey, String metadataEquals) {
        if (!rule.eventTypes().contains(candidate.eventType())) {
            return false;
        }
        if (metadataKey == null) {
            return true;
        }
        return metadataEquals.equals(candidate.metadata().get(metadataKey));
    }
}
