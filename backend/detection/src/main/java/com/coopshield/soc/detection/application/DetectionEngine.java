package com.coopshield.soc.detection.application;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Avalia um evento normalizado contra todas as regras habilitadas cujo
 * {@code eventTypes} o cobre, usando o historico recente relevante (por
 * ator ou por dispositivo, conforme {@code conditions.aggregationKey} da
 * regra). Correspondencias sao persistidas e publicadas; o evento so entra
 * no historico depois de avaliado, para que as regras vejam apenas o que
 * aconteceu ANTES dele.
 */
public class DetectionEngine {

    private final List<DetectionRule> rules;
    private final Map<String, RuleEvaluator> evaluatorsByType;
    private final EventHistory history;
    private final DetectionMatchRepository matchRepository;
    private final DetectionMatchPublisher publisher;

    public DetectionEngine(List<DetectionRule> rules, List<RuleEvaluator> evaluators, EventHistory history,
                            DetectionMatchRepository matchRepository, DetectionMatchPublisher publisher) {
        this.rules = List.copyOf(rules);
        this.evaluatorsByType = evaluators.stream()
                .collect(Collectors.toMap(RuleEvaluator::type, e -> e));
        this.history = history;
        this.matchRepository = matchRepository;
        this.publisher = publisher;
    }

    public List<DetectionMatch> evaluate(EventEnvelope event) {
        List<DetectionMatch> matches = new ArrayList<>();

        for (DetectionRule rule : rules) {
            if (!rule.appliesTo(event.eventType())) {
                continue;
            }
            RuleEvaluator evaluator = evaluatorsByType.get(rule.evaluatorType());
            if (evaluator == null) {
                throw new IllegalStateException("No RuleEvaluator registered for type " + rule.evaluatorType());
            }

            List<EventEnvelope> relevantHistory = "device".equals(rule.conditions().get("aggregationKey"))
                    ? history.byDevice(event.device().deviceId(), rule.aggregationWindow(), event.timestamp())
                    : history.byActor(event.actor().userId(), rule.aggregationWindow(), event.timestamp());

            evaluator.evaluate(rule, event, relevantHistory).ifPresent(matches::add);
        }

        history.record(event);

        for (DetectionMatch match : matches) {
            matchRepository.save(match);
            publisher.publish(match);
        }

        return matches;
    }
}
