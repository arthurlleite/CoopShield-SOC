package com.coopshield.soc.detection.application;

import com.coopshield.soc.detection.domain.DetectionMatch;
import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.RuleEvaluator;
import com.coopshield.soc.detection.domain.TestEvents;
import com.coopshield.soc.detection.domain.evaluator.AtypicalHourEvaluator;
import com.coopshield.soc.detection.domain.evaluator.FailureThenSuccessEvaluator;
import com.coopshield.soc.detection.domain.evaluator.ImpossibleTravelEvaluator;
import com.coopshield.soc.detection.domain.evaluator.SameDeviceMultipleAccountsEvaluator;
import com.coopshield.soc.detection.domain.evaluator.SequenceTwoTypesEvaluator;
import com.coopshield.soc.detection.domain.evaluator.SingleEventFlagEvaluator;
import com.coopshield.soc.detection.domain.evaluator.ThresholdCountEvaluator;
import com.coopshield.soc.detection.infrastructure.history.InMemoryEventHistory;
import com.coopshield.soc.detection.infrastructure.yaml.DetectionRuleYamlLoader;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa cada uma das 15 regras iniciais contra o motor real (regras
 * carregadas de YAML, avaliadores reais, historico em memoria real),
 * cobrindo tanto o caso em que a regra deve acionar quanto, quando
 * relevante, o caso em que nao deve (evitando falso positivo).
 */
class DetectionEngineTest {

    private DetectionEngine engine;
    private DetectionMatchRepository matchRepository;
    private DetectionMatchPublisher publisher;

    @BeforeEach
    void setUp() {
        List<DetectionRule> rules = new DetectionRuleYamlLoader().loadAll();
        List<RuleEvaluator> evaluators = List.of(
                new ThresholdCountEvaluator(), new FailureThenSuccessEvaluator(), new SingleEventFlagEvaluator(),
                new AtypicalHourEvaluator(), new ImpossibleTravelEvaluator(), new SameDeviceMultipleAccountsEvaluator(),
                new SequenceTwoTypesEvaluator());
        matchRepository = Mockito.mock(DetectionMatchRepository.class);
        publisher = Mockito.mock(DetectionMatchPublisher.class);
        engine = new DetectionEngine(rules, evaluators, new InMemoryEventHistory(), matchRepository, publisher);
    }

    private List<DetectionMatch> matchesOf(String ruleId, List<DetectionMatch> matches) {
        return matches.stream().filter(m -> m.ruleId().equals(ruleId)).toList();
    }

    @Test
    void rule001FiresOnFourFailuresFollowedBySuccessButNotOnThree() {
        String actorWithThreeFailures = "actor-001a";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.failure")
                    .actor(actorWithThreeFailures, "EMPLOYEE").outcome(Outcome.FAILURE).build()));
        }
        collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor(actorWithThreeFailures, "EMPLOYEE").outcome(Outcome.SUCCESS).build()));
        assertThat(matchesOf("RULE-001", collected)).isEmpty();

        // Ator distinto: a 4a falha, seguida de sucesso, deve acionar.
        String actorWithFourFailures = "actor-001b";
        collected.clear();
        for (int i = 0; i < 4; i++) {
            collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.failure")
                    .actor(actorWithFourFailures, "EMPLOYEE").outcome(Outcome.FAILURE).build()));
        }
        collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor(actorWithFourFailures, "EMPLOYEE").outcome(Outcome.SUCCESS).build()));

        List<DetectionMatch> rule001 = matchesOf("RULE-001", collected);
        assertThat(rule001).hasSize(1);
        assertThat(rule001.get(0).observedValue()).isEqualTo(4);
        assertThat(rule001.get(0).evidenceEventIds()).hasSize(5);
        assertThat(rule001.get(0).mitreTechnique()).contains("T1110");
    }

    @Test
    void rule002FiresOutsideBusinessHoursButNotDuringThem() {
        List<DetectionMatch> daytime = engine.evaluate(TestEvents.builder("authentication.login.success")
                .metadata(Map.of("hourOfDay", "14")).build());
        assertThat(matchesOf("RULE-002", daytime)).isEmpty();

        List<DetectionMatch> nighttime = engine.evaluate(TestEvents.builder("authentication.login.success")
                .metadata(Map.of("hourOfDay", "03")).build());
        assertThat(matchesOf("RULE-002", nighttime)).hasSize(1);
    }

    @Test
    void rule003FiresOnUnrecognizedDevice() {
        List<DetectionMatch> matches = engine.evaluate(TestEvents.builder("device.unrecognized")
                .device("synthetic-device-unknown", false).build());

        assertThat(matchesOf("RULE-003", matches)).hasSize(1);
    }

    @Test
    void rule004FiresOnGeoChangeBetweenConsecutiveLoginsButNotOnSameGeo() {
        String actor = "actor-004";
        engine.evaluate(TestEvents.builder("authentication.login.success").actor(actor, "EMPLOYEE").geo("region-a").build());
        List<DetectionMatch> sameGeo = engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor(actor, "EMPLOYEE").geo("region-a").build());
        assertThat(matchesOf("RULE-004", sameGeo)).isEmpty();

        List<DetectionMatch> differentGeo = engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor(actor, "EMPLOYEE").geo("region-b").build());
        assertThat(matchesOf("RULE-004", differentGeo)).hasSize(1);
    }

    @Test
    void rule005FiresOnAnyAccessDeniedEvent() {
        List<DetectionMatch> matches = engine.evaluate(TestEvents.builder("authorization.access.denied").build());

        assertThat(matchesOf("RULE-005", matches)).hasSize(1);
    }

    @Test
    void rule006FiresOnThirdDenialButNotSecond() {
        String actor = "actor-006";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        collected.addAll(engine.evaluate(TestEvents.builder("authorization.access.denied").actor(actor, "EMPLOYEE").build()));
        collected.addAll(engine.evaluate(TestEvents.builder("authorization.access.denied").actor(actor, "EMPLOYEE").build()));
        assertThat(matchesOf("RULE-006", collected)).isEmpty();

        collected.addAll(engine.evaluate(TestEvents.builder("authorization.access.denied").actor(actor, "EMPLOYEE").build()));
        assertThat(matchesOf("RULE-006", collected)).hasSize(1);
    }

    @Test
    void rule007FiresOnFifteenthQueryButNotFourteenth() {
        String actor = "actor-007";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        for (int i = 0; i < 14; i++) {
            collected.addAll(engine.evaluate(TestEvents.builder("data.access.query").actor(actor, "EMPLOYEE").build()));
        }
        assertThat(matchesOf("RULE-007", collected)).isEmpty();

        collected.addAll(engine.evaluate(TestEvents.builder("data.access.query").actor(actor, "EMPLOYEE").build()));
        List<DetectionMatch> rule007 = matchesOf("RULE-007", collected);
        assertThat(rule007).hasSize(1);
        assertThat(rule007.get(0).observedValue()).isEqualTo(15);
    }

    @Test
    void rule008FiresOnSecondExportButNotFirst() {
        String actor = "actor-008";
        List<DetectionMatch> first = engine.evaluate(TestEvents.builder("data.access.export").actor(actor, "EMPLOYEE").build());
        assertThat(matchesOf("RULE-008", first)).isEmpty();

        List<DetectionMatch> second = engine.evaluate(TestEvents.builder("data.access.export").actor(actor, "EMPLOYEE").build());
        assertThat(matchesOf("RULE-008", second)).hasSize(1);
    }

    @Test
    void rule009FiresOnDataAccessAfterAdminChangeButNotWithoutIt() {
        String actorWithoutAdmin = "actor-009a";
        List<DetectionMatch> withoutAdmin = engine.evaluate(TestEvents.builder("data.access.query")
                .actor(actorWithoutAdmin, "IT_ADMIN").build());
        assertThat(matchesOf("RULE-009", withoutAdmin)).isEmpty();

        String actorWithAdmin = "actor-009b";
        engine.evaluate(TestEvents.builder("admin.permission.changed").actor(actorWithAdmin, "IT_ADMIN").build());
        List<DetectionMatch> afterAdmin = engine.evaluate(TestEvents.builder("data.access.query")
                .actor(actorWithAdmin, "IT_ADMIN").build());
        assertThat(matchesOf("RULE-009", afterAdmin)).hasSize(1);
    }

    @Test
    void rule010FiresOnSensitiveExposure() {
        List<DetectionMatch> matches = engine.evaluate(TestEvents.builder("data.access.sensitive.exposure").build());

        List<DetectionMatch> rule010 = matchesOf("RULE-010", matches);
        assertThat(rule010).hasSize(1);
        assertThat(rule010.get(0).severity().name()).isEqualTo("CRITICAL");
    }

    @Test
    void rule011FiresOnFourth401ButRule012DoesNot() {
        String actor = "actor-011";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            collected.addAll(engine.evaluate(TestEvents.builder("api.response.error")
                    .actor(actor, "EMPLOYEE").outcome(Outcome.FAILURE).metadata(Map.of("httpStatus", "401")).build()));
        }
        assertThat(matchesOf("RULE-011", collected)).hasSize(1);
        assertThat(matchesOf("RULE-012", collected)).isEmpty();
    }

    @Test
    void rule012FiresOnFourth403ButRule011DoesNot() {
        String actor = "actor-012";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            collected.addAll(engine.evaluate(TestEvents.builder("api.response.error")
                    .actor(actor, "EMPLOYEE").outcome(Outcome.FAILURE).metadata(Map.of("httpStatus", "403")).build()));
        }
        assertThat(matchesOf("RULE-012", collected)).hasSize(1);
        assertThat(matchesOf("RULE-011", collected)).isEmpty();
    }

    @Test
    void rule013FiresOnlyForPrivilegedRoleWithOutOfPatternFlag() {
        List<DetectionMatch> notPrivileged = engine.evaluate(TestEvents.builder("data.access.export")
                .actor("actor-013a", "EMPLOYEE").metadata(Map.of("outOfPattern", "true")).build());
        assertThat(matchesOf("RULE-013", notPrivileged)).isEmpty();

        List<DetectionMatch> privilegedButInPattern = engine.evaluate(TestEvents.builder("data.access.export")
                .actor("actor-013b", "IT_ADMIN").metadata(Map.of()).build());
        assertThat(matchesOf("RULE-013", privilegedButInPattern)).isEmpty();

        List<DetectionMatch> privilegedOutOfPattern = engine.evaluate(TestEvents.builder("data.access.export")
                .actor("actor-013c", "IT_ADMIN").metadata(Map.of("outOfPattern", "true")).build());
        assertThat(matchesOf("RULE-013", privilegedOutOfPattern)).hasSize(1);
    }

    @Test
    void rule014FiresOnSecondAdminActionButNotFirst() {
        String actor = "actor-014";
        List<DetectionMatch> first = engine.evaluate(TestEvents.builder("admin.permission.changed").actor(actor, "IT_ADMIN").build());
        assertThat(matchesOf("RULE-014", first)).isEmpty();

        List<DetectionMatch> second = engine.evaluate(TestEvents.builder("admin.account.created").actor(actor, "IT_ADMIN").build());
        assertThat(matchesOf("RULE-014", second)).hasSize(1);
    }

    @Test
    void rule015FiresOnThirdDistinctActorSharingDeviceButNotSecond() {
        String device = "synthetic-device-shared";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor("actor-015a", "EMPLOYEE").device(device, true).build()));
        collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor("actor-015b", "EMPLOYEE").device(device, true).build()));
        assertThat(matchesOf("RULE-015", collected)).isEmpty();

        collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor("actor-015c", "EMPLOYEE").device(device, true).build()));
        List<DetectionMatch> rule015 = matchesOf("RULE-015", collected);
        assertThat(rule015).hasSize(1);
        assertThat(rule015.get(0).observedValue()).isEqualTo(3);
    }

    @Test
    void normalActivityTriggersNoMatches() {
        CorrelationId correlationId = CorrelationId.newId();
        String actor = "actor-normal";
        List<DetectionMatch> collected = new java.util.ArrayList<>();
        collected.addAll(engine.evaluate(TestEvents.builder("authentication.login.success")
                .actor(actor, "EMPLOYEE").correlationId(correlationId).metadata(Map.of("hourOfDay", "10")).build()));
        collected.addAll(engine.evaluate(TestEvents.builder("data.access.query")
                .actor(actor, "EMPLOYEE").correlationId(correlationId).build()));
        collected.addAll(engine.evaluate(TestEvents.builder("authentication.logout")
                .actor(actor, "EMPLOYEE").correlationId(correlationId).build()));

        List<DetectionMatch> forThisRun = collected.stream()
                .filter(m -> m.correlationId().equals(correlationId.toString())).toList();
        assertThat(forThisRun).isEmpty();
    }

    @Test
    void matchesArePersistedAndPublished() {
        List<DetectionMatch> matches = engine.evaluate(TestEvents.builder("data.access.sensitive.exposure").build());

        assertThat(matches).isNotEmpty();
        Mockito.verify(matchRepository, Mockito.atLeastOnce()).save(Mockito.any());
        Mockito.verify(publisher, Mockito.atLeastOnce()).publish(Mockito.any());
    }
}
