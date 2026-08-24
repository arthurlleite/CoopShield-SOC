package com.coopshield.soc.detection.domain.evaluator;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * RULE-003, RULE-005, RULE-010, RULE-013: aciona no proprio evento, sem
 * agregacao, desde que ele satisfaca os filtros opcionais em
 * {@code conditions}:
 * <ul>
 *   <li>{@code metadataKey}/{@code metadataEquals} - RULE-013 (flag de fora
 *       do padrao);</li>
 *   <li>{@code deviceKnownEquals} - reservado para regras futuras baseadas
 *       em dispositivo conhecido/desconhecido;</li>
 *   <li>{@code privilegedRolesIn} - RULE-013 (apenas perfis privilegiados).</li>
 * </ul>
 * Quando nenhuma condicao extra e declarada (RULE-003, RULE-005, RULE-010),
 * o proprio {@code eventType} da regra ja e o sinal suficiente.
 */
public class SingleEventFlagEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "single-event-flag";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        String metadataKey = rule.conditions().get("metadataKey");
        String metadataEquals = rule.conditions().get("metadataEquals");
        if (metadataKey != null && !metadataEquals.equals(event.metadata().get(metadataKey))) {
            return Optional.empty();
        }

        String deviceKnownEquals = rule.conditions().get("deviceKnownEquals");
        if (deviceKnownEquals != null && event.device().known() != Boolean.parseBoolean(deviceKnownEquals)) {
            return Optional.empty();
        }

        String privilegedRolesIn = rule.conditions().get("privilegedRolesIn");
        if (privilegedRolesIn != null) {
            Set<String> privilegedRoles = Set.of(privilegedRolesIn.split(","));
            if (!privilegedRoles.contains(event.actor().role())) {
                return Optional.empty();
            }
        }

        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                1, 1, List.of(event.eventId().toString()), rule.severity(), rule.baseRiskScore(),
                rule.mitreTactic(), rule.mitreTechnique(), rule.recommendedPlaybook(),
                event.correlationId().toString(), Instant.now()));
    }
}
