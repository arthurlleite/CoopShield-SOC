package com.coopshield.soc.detection.domain.evaluator;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RULE-002: aciona quando o evento carrega o indicador sintetico
 * {@code metadata.hourOfDay} (gerado pelo simulador, ver
 * {@code Scenario.ATYPICAL_AUTH}) fora do intervalo declarado em
 * {@code conditions.businessHourStart}/{@code businessHourEnd}. Um relogio
 * de parede real seria a fonte natural dessa informacao em producao; usar
 * um indicador explicito no evento sintetico evita depender do horario em
 * que os testes/demonstracoes rodam.
 */
public class AtypicalHourEvaluator implements RuleEvaluator {

    @Override
    public String type() {
        return "atypical-hour";
    }

    @Override
    public Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory) {
        String hourOfDay = event.metadata().get("hourOfDay");
        if (hourOfDay == null) {
            return Optional.empty();
        }

        int hour = Integer.parseInt(hourOfDay);
        int businessStart = Integer.parseInt(rule.conditions().get("businessHourStart"));
        int businessEnd = Integer.parseInt(rule.conditions().get("businessHourEnd"));
        if (hour >= businessStart && hour < businessEnd) {
            return Optional.empty();
        }

        return Optional.of(new DetectionMatch(
                UUID.randomUUID(), rule.id(), rule.name(), event.actor().userId(),
                hour, businessStart, List.of(event.eventId().toString()), rule.severity(), rule.baseRiskScore(),
                rule.mitreTactic(), rule.mitreTechnique(), rule.recommendedPlaybook(),
                event.correlationId().toString(), Instant.now()));
    }
}
