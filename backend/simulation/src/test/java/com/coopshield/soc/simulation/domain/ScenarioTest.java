package com.coopshield.soc.simulation.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioTest {

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void generatesExactlyTheDefaultEventCountAtDefault(Scenario scenario) {
        List<GeneratedEvent> events = scenario.generate(Characters.ANA_BEATRIZ, scenario.defaultEventCount());

        assertThat(events).hasSize(scenario.defaultEventCount());
        assertThat(events).allSatisfy(event -> {
            assertThat(event.eventType()).isNotBlank();
            assertThat(event.action()).isNotBlank();
            assertThat(event.outcome()).isIn("SUCCESS", "FAILURE");
            assertThat(event.sourceIp()).isNotBlank();
            assertThat(event.deviceId()).isNotBlank();
        });
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void neverGeneratesMoreEventsThanRequested(Scenario scenario) {
        List<GeneratedEvent> events = scenario.generate(Characters.FERNANDA_LIMA, 1);

        assertThat(events).hasSizeLessThanOrEqualTo(1);
    }

    @Test
    void everyScenarioHasAUniqueIdMatchingTheEventCatalogNaming() {
        Set<String> ids = java.util.Arrays.stream(Scenario.values()).map(Scenario::id).collect(java.util.stream.Collectors.toSet());

        assertThat(ids).hasSize(Scenario.values().length);
        assertThat(ids).allSatisfy(id -> assertThat(id).matches("[a-z0-9-]+"));
    }

    @Test
    void findByIdResolvesAllTwelveScenarios() {
        assertThat(Scenario.values()).hasSize(12);
        for (Scenario scenario : Scenario.values()) {
            assertThat(Scenario.findById(scenario.id())).contains(scenario);
        }
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(Scenario.findById("does-not-exist")).isEmpty();
    }

    @Test
    void accountCompromisedEndsWithASuccessfulLoginAfterFailures() {
        List<GeneratedEvent> events = Scenario.ACCOUNT_COMPROMISED.generate(Characters.ROBERTO_NOGUEIRA, 6);

        assertThat(events.get(events.size() - 1).eventType()).isEqualTo("authentication.login.success");
        assertThat(events.stream().filter(e -> e.eventType().equals("authentication.login.failure")).count())
                .isGreaterThanOrEqualTo(3);
        assertThat(events).anySatisfy(e -> assertThat(e.eventType()).isEqualTo("device.unrecognized"));
        assertThat(events).allSatisfy(e -> assertThat(e.deviceKnown()).isFalse());
    }

    @Test
    void neverEmbedsAPlausibleRealLookingDocumentNumberInMetadata() {
        List<GeneratedEvent> events = Scenario.PII_EXPOSED.generate(Characters.MARINA_SOUZA, 2);

        assertThat(events).allSatisfy(event ->
                event.metadata().values().forEach(value -> assertThat(value).doesNotMatch(".*\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}.*")));
    }
}
