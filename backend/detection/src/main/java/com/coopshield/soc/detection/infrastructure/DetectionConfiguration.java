package com.coopshield.soc.detection.infrastructure;

import com.coopshield.soc.detection.application.DetectionEngine;
import com.coopshield.soc.detection.application.DetectionMatchPublisher;
import com.coopshield.soc.detection.application.DetectionMatchRepository;
import com.coopshield.soc.detection.application.DetectionRuleRepository;
import com.coopshield.soc.detection.application.EventHistory;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.detection.domain.evaluator.AtypicalHourEvaluator;
import com.coopshield.soc.detection.domain.evaluator.FailureThenSuccessEvaluator;
import com.coopshield.soc.detection.domain.evaluator.ImpossibleTravelEvaluator;
import com.coopshield.soc.detection.domain.evaluator.SameDeviceMultipleAccountsEvaluator;
import com.coopshield.soc.detection.domain.evaluator.SequenceTwoTypesEvaluator;
import com.coopshield.soc.detection.domain.evaluator.SingleEventFlagEvaluator;
import com.coopshield.soc.detection.domain.evaluator.ThresholdCountEvaluator;
import com.coopshield.soc.detection.infrastructure.kafka.DetectionTopics;
import com.coopshield.soc.detection.infrastructure.yaml.DetectionRuleYamlLoader;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Composicao dos beans do modulo detection: carrega as regras de YAML uma
 * unica vez na inicializacao, registra os sete avaliadores (ver ADR-014),
 * liga o motor de deteccao as portas de aplicacao e declara o topico que
 * este modulo produz.
 */
@Configuration
public class DetectionConfiguration {

    @Bean
    public List<DetectionRule> detectionRules(DetectionRuleRepository ruleRepository) {
        List<DetectionRule> rules = new DetectionRuleYamlLoader().loadAll();
        ruleRepository.saveAll(rules);
        return rules;
    }

    @Bean
    public List<RuleEvaluator> ruleEvaluators() {
        return List.of(
                new ThresholdCountEvaluator(),
                new FailureThenSuccessEvaluator(),
                new SingleEventFlagEvaluator(),
                new AtypicalHourEvaluator(),
                new ImpossibleTravelEvaluator(),
                new SameDeviceMultipleAccountsEvaluator(),
                new SequenceTwoTypesEvaluator());
    }

    @Bean
    public DetectionEngine detectionEngine(List<DetectionRule> detectionRules, List<RuleEvaluator> ruleEvaluators,
                                            EventHistory eventHistory, DetectionMatchRepository matchRepository,
                                            DetectionMatchPublisher publisher) {
        return new DetectionEngine(detectionRules, ruleEvaluators, eventHistory, matchRepository, publisher);
    }

    @Bean
    public NewTopic detectionAlertsTopic() {
        return TopicBuilder.name(DetectionTopics.DETECTION_ALERTS).partitions(3).replicas(1).build();
    }

    /**
     * Falha rapido se alguma regra referenciar um {@code evaluatorType}
     * desconhecido - preferivel a descobrir isso silenciosamente quando o
     * primeiro evento daquele tipo chegar.
     */
    @Bean
    public ApplicationRunner validateEvaluatorTypesAtStartup(List<DetectionRule> detectionRules, List<RuleEvaluator> ruleEvaluators) {
        return (ApplicationArguments args) -> {
            Set<String> knownTypes = ruleEvaluators.stream().map(RuleEvaluator::type).collect(Collectors.toSet());
            for (DetectionRule rule : detectionRules) {
                if (!knownTypes.contains(rule.evaluatorType())) {
                    throw new IllegalStateException(
                            "Rule " + rule.id() + " references unknown evaluatorType " + rule.evaluatorType());
                }
            }
        };
    }
}
