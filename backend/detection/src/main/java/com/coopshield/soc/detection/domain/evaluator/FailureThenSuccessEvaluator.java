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
 * RULE-001: aciona quando o evento atual e um login bem-sucedido
 * ({@code conditions.successEventType}) precedido, na janela de agregacao,
 * por ao menos {@code threshold} falhas ({@code conditions.failureEventType})
 * do mesmo ator - a jornada de referencia da Fase 0 ("conta possivelmente
 * comprometida").
 */
public class FailureThenSuccessEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "failure-then-success";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        String successEventType = rule.conditions().get("successEventType");
        String failureEventType = rule.conditions().get("failureEventType");

        if (!event.eventType().equals(successEventType)) {
            return Optional.empty();
        }

        List<EventEnvelope> failures = new ArrayList<>();
        for (EventEnvelope candidate : recentHistory) {
            if (candidate.eventType().equals(failureEventType)) {
                failures.add(candidate);
            }
        }

        if (failures.size() < rule.threshold()) {
            return Optional.empty();
        }

        List<String> evidence = new ArrayList<>(failures.stream().map(e -> e.eventId().toString()).toList());
        evidence.add(event.eventId().toString());

        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                failures.size(), rule.threshold(), evidence, rule.severity(), rule.baseRiskScore(),
                rule.mitreTactic(), rule.mitreTechnique(), rule.recommendedPlaybook(),
                event.correlationId().toString(), Instant.now()));
    }
}
