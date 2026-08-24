package com.coopshield.soc.detection.domain.evaluator;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * RULE-015: aciona quando o mesmo {@code deviceId} e usado por
 * {@code threshold} ou mais atores distintos dentro da janela de agregacao -
 * {@code recentHistory} aqui e o historico POR DISPOSITIVO (nao por ator),
 * ver {@code conditions.aggregationKey=device} e
 * {@code DetectionEngine}/{@code EventHistory}.
 */
public class SameDeviceMultipleAccountsEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "same-device-multiple-accounts";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        Map<String, String> latestEventIdByActor = new LinkedHashMap<>();
        for (EventEnvelope candidate : recentHistory) {
            if (rule.eventTypes().contains(candidate.eventType())) {
                latestEventIdByActor.put(candidate.actor().userId(), candidate.eventId().toString());
            }
        }
        latestEventIdByActor.put(event.actor().userId(), event.eventId().toString());

        if (latestEventIdByActor.size() < rule.threshold()) {
            return Optional.empty();
        }

        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                latestEventIdByActor.size(), rule.threshold(), List.copyOf(latestEventIdByActor.values()),
                rule.severity(), rule.baseRiskScore(), rule.mitreTactic(), rule.mitreTechnique(),
                rule.recommendedPlaybook(), event.correlationId().toString(), Instant.now()));
    }
}
