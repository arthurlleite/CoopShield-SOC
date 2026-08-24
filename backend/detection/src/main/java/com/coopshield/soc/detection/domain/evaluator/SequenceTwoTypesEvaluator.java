package com.coopshield.soc.detection.domain.evaluator;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * RULE-009: aciona quando o evento atual (um dos {@code eventTypes} da
 * regra) e precedido, na janela de agregacao, por um evento de um dos tipos
 * em {@code conditions.firstEventTypes} do mesmo ator - "alteracao
 * administrativa seguida de acesso a dado sensivel".
 */
public class SequenceTwoTypesEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "sequence-two-types";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        Set<String> firstEventTypes = Set.of(rule.conditions().get("firstEventTypes").split(","));

        Optional<EventEnvelope> firstEvent = recentHistory.stream()
                .filter(candidate -> firstEventTypes.contains(candidate.eventType()))
                .max(Comparator.comparing(EventEnvelope::timestamp));

        if (firstEvent.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                1, 1, List.of(firstEvent.get().eventId().toString(), event.eventId().toString()),
                rule.severity(), rule.baseRiskScore(), rule.mitreTactic(), rule.mitreTechnique(),
                rule.recommendedPlaybook(), event.correlationId().toString(), Instant.now()));
    }
}
