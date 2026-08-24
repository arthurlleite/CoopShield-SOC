package com.coopshield.soc.simulation.application;

import com.coopshield.soc.eventingestion.application.EventIngestionService;
import com.coopshield.soc.eventingestion.application.RawEventPublisher;
import com.coopshield.soc.simulation.domain.Characters;
import com.coopshield.soc.simulation.domain.InvalidEventCountException;
import com.coopshield.soc.simulation.domain.Scenario;
import com.coopshield.soc.simulation.domain.SimulationRun;
import com.coopshield.soc.simulation.domain.SimulationRunStatus;
import com.coopshield.soc.simulation.domain.UnknownCharacterException;
import com.coopshield.soc.simulation.domain.UnknownScenarioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private RawEventPublisher publisher;
    @Mock
    private SimulationRunRepository repository;

    private SimulationService service;

    @BeforeEach
    void setUp() {
        service = new SimulationService(new EventIngestionService(publisher), repository);
    }

    @Test
    void startsARunPublishesAllEventsAndMarksItCompleted() {
        SimulationRun run = service.start(Scenario.NORMAL.id(), Characters.ANA_BEATRIZ.id(), null);

        assertThat(run.status()).isEqualTo(SimulationRunStatus.COMPLETED);
        assertThat(run.publishedEventCount()).isEqualTo(Scenario.NORMAL.defaultEventCount());
        assertThat(run.scenarioId()).isEqualTo(Scenario.NORMAL.id());
        assertThat(run.characterId()).isEqualTo(Characters.ANA_BEATRIZ.id());
        verify(publisher, times(Scenario.NORMAL.defaultEventCount())).publish(any());
        verify(repository, times(2)).save(any());
    }

    @Test
    void everyPublishedEventSharesTheRunsCorrelationId() {
        SimulationRun run = service.start(Scenario.MASS_QUERY.id(), Characters.FERNANDA_LIMA.id(), 5);

        ArgumentCaptor<com.coopshield.soc.eventingestion.domain.RawEvent> captor =
                ArgumentCaptor.forClass(com.coopshield.soc.eventingestion.domain.RawEvent.class);
        verify(publisher, times(5)).publish(captor.capture());
        assertThat(captor.getAllValues())
                .allSatisfy(event -> assertThat(event.correlationId().toString()).isEqualTo(run.correlationId().toString()));
    }

    @Test
    void rejectsUnknownScenario() {
        assertThatThrownBy(() -> service.start("not-a-scenario", Characters.ANA_BEATRIZ.id(), null))
                .isInstanceOf(UnknownScenarioException.class);

        verify(publisher, never()).publish(any());
    }

    @Test
    void rejectsUnknownCharacter() {
        assertThatThrownBy(() -> service.start(Scenario.NORMAL.id(), "not-a-character", null))
                .isInstanceOf(UnknownCharacterException.class);

        verify(publisher, never()).publish(any());
    }

    @Test
    void rejectsEventCountOutsideBounds() {
        assertThatThrownBy(() -> service.start(Scenario.NORMAL.id(), Characters.ANA_BEATRIZ.id(), 0))
                .isInstanceOf(InvalidEventCountException.class);
        assertThatThrownBy(() -> service.start(Scenario.NORMAL.id(), Characters.ANA_BEATRIZ.id(), 1000))
                .isInstanceOf(InvalidEventCountException.class);
    }
}
