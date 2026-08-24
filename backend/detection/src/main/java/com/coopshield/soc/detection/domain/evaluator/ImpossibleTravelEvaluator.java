package com.coopshield.soc.detection.domain.evaluator;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RULE-004: aciona quando o evento atual e o login bem-sucedido mais recente
 * do ator tem uma regiao sintetica ({@code networkContext.geo}) diferente da
 * do login bem-sucedido anterior dentro da janela de agregacao -
 * simplificacao deliberada de "viagem impossivel" adequada a dados
 * sinteticos (sem calculo real de distancia/velocidade).
 */
public class ImpossibleTravelEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "impossible-travel";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        Optional<EventEnvelope> previous = recentHistory.stream()
                .filter(candidate -> rule.eventTypes().contains(candidate.eventType()))
                .max(Comparator.comparing(EventEnvelope::timestamp));

        if (previous.isEmpty()) {
            return Optional.empty();
        }
        if (previous.get().networkContext().geo().equals(event.networkContext().geo())) {
            return Optional.empty();
        }

        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                1, 1, List.of(previous.get().eventId().toString(), event.eventId().toString()),
                rule.severity(), rule.baseRiskScore(), rule.mitreTactic(), rule.mitreTechnique(),
                rule.recommendedPlaybook(), event.correlationId().toString(), Instant.now()));
    }
}
