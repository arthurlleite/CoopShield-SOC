package com.coopshield.soc.detection.infrastructure.yaml;

import com.coopshield.soc.detection.domain.DetectionRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DetectionRuleYamlLoaderTest {

    private final DetectionRuleYamlLoader loader = new DetectionRuleYamlLoader();

    @Test
    void loadsAllFifteenInitialRules() {
        List<DetectionRule> rules = loader.loadAll();

        assertThat(rules).hasSize(15);
        Set<String> ids = rules.stream().map(DetectionRule::id).collect(java.util.stream.Collectors.toSet());
        for (int i = 1; i <= 15; i++) {
            assertThat(ids).contains(String.format("RULE-%03d", i));
        }
    }

    @Test
    void everyRuleIsEnabledHasAtLeastOneEventTypeAndAMitreMapping() {
        for (DetectionRule rule : loader.loadAll()) {
            assertThat(rule.enabled()).as("rule %s enabled", rule.id()).isTrue();
            assertThat(rule.eventTypes()).as("rule %s eventTypes", rule.id()).isNotEmpty();
            assertThat(rule.mitreTactic()).as("rule %s mitreTactic", rule.id()).isNotBlank();
            assertThat(rule.mitreTechnique()).as("rule %s mitreTechnique", rule.id()).isNotBlank();
            assertThat(rule.recommendedPlaybook()).as("rule %s recommendedPlaybook", rule.id()).isNotBlank();
            assertThat(rule.falsePositiveNotes()).as("rule %s falsePositiveNotes", rule.id()).isNotBlank();
        }
    }

    @Test
    void loadingIsDeterministicAndSortedById() {
        List<DetectionRule> rules = loader.loadAll();

        List<String> ids = rules.stream().map(DetectionRule::id).toList();
        assertThat(ids).isSorted();
    }
}
