package com.coopshield.soc.simulation.application;

import com.coopshield.soc.eventingestion.application.EventIngestionService;
import com.coopshield.soc.simulation.domain.Character;
import com.coopshield.soc.simulation.domain.Characters;
import com.coopshield.soc.simulation.domain.GeneratedEvent;
import com.coopshield.soc.simulation.domain.InvalidEventCountException;
import com.coopshield.soc.simulation.domain.Scenario;
import com.coopshield.soc.simulation.domain.SimulationRun;
import com.coopshield.soc.simulation.domain.UnknownCharacterException;
import com.coopshield.soc.simulation.domain.UnknownScenarioException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Orquestra uma execucao do laboratorio: resolve personagem/cenario, gera os
 * eventos e os publica atraves de {@link EventIngestionService} - o mesmo
 * caminho de validacao/publicacao usado por {@code POST /api/v1/events} (ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md), garantindo que
 * eventos sinteticos e eventos reais de API passem exatamente pelas mesmas
 * regras.
 */
public class SimulationService {

    static final int MIN_EVENT_COUNT = 1;
    static final int MAX_EVENT_COUNT = 100;

    private final EventIngestionService ingestionService;
    private final SimulationRunRepository repository;

    public SimulationService(EventIngestionService ingestionService, SimulationRunRepository repository) {
        this.ingestionService = ingestionService;
        this.repository = repository;
    }

    public List<Scenario> scenarios() {
        return List.of(Scenario.values());
    }

    public List<Character> characters() {
        return Characters.all();
    }

    public SimulationRun start(String scenarioId, String characterId, Integer requestedEventCount) {
        Scenario scenario = Scenario.findById(scenarioId).orElseThrow(() -> new UnknownScenarioException(scenarioId));
        Character actor = Characters.findById(characterId).orElseThrow(() -> new UnknownCharacterException(characterId));
        int eventCount = requestedEventCount == null ? scenario.defaultEventCount() : requestedEventCount;
        if (eventCount < MIN_EVENT_COUNT || eventCount > MAX_EVENT_COUNT) {
            throw new InvalidEventCountException(eventCount, MIN_EVENT_COUNT, MAX_EVENT_COUNT);
        }

        UUID runId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        SimulationRun run = new SimulationRun(runId, scenario.id(), actor.id(), correlationId, eventCount, Instant.now());
        repository.save(run);

        try {
            List<GeneratedEvent> events = scenario.generate(actor, eventCount);
            for (GeneratedEvent event : events) {
                ingestionService.ingest(
                        null, null, event.eventType(), null, "simulation",
                        actor.userId(), actor.role(), actor.unit(),
                        event.targetResourceType(), event.targetResourceId(),
                        event.action(), event.outcome(), event.deviceId(), event.deviceKnown(),
                        event.sourceIp(), event.geo(), correlationId.toString(), event.metadata());
            }
            run.complete(events.size(), Instant.now());
        } catch (RuntimeException e) {
            run.fail(run.publishedEventCount(), Instant.now());
            repository.save(run);
            throw e;
        }

        repository.save(run);
        return run;
    }
}
