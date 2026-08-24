package com.coopshield.soc.simulation.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de uma execucao do laboratorio: qual cenario, qual personagem,
 * quantos eventos foram solicitados/publicados, e o {@code correlationId}
 * compartilhado por todos os eventos gerados nesta execucao - permite
 * reconstruir a jornada completa (ver docs/event-catalog/events.md).
 */
public final class SimulationRun {

    private final UUID runId;
    private final String scenarioId;
    private final String characterId;
    private final UUID correlationId;
    private final int requestedEventCount;
    private final Instant startedAt;
    private int publishedEventCount;
    private Instant completedAt;
    private SimulationRunStatus status;

    public SimulationRun(UUID runId, String scenarioId, String characterId, UUID correlationId,
                          int requestedEventCount, Instant startedAt) {
        this.runId = Objects.requireNonNull(runId, "runId must not be null");
        this.scenarioId = Objects.requireNonNull(scenarioId, "scenarioId must not be null");
        this.characterId = Objects.requireNonNull(characterId, "characterId must not be null");
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId must not be null");
        this.requestedEventCount = requestedEventCount;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        this.publishedEventCount = 0;
        this.status = SimulationRunStatus.RUNNING;
    }

    public static SimulationRun rehydrate(UUID runId, String scenarioId, String characterId, UUID correlationId,
                                           int requestedEventCount, int publishedEventCount, Instant startedAt,
                                           Instant completedAt, SimulationRunStatus status) {
        SimulationRun run = new SimulationRun(runId, scenarioId, characterId, correlationId, requestedEventCount, startedAt);
        run.publishedEventCount = publishedEventCount;
        run.completedAt = completedAt;
        run.status = status;
        return run;
    }

    public void complete(int publishedEventCount, Instant completedAt) {
        this.publishedEventCount = publishedEventCount;
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt must not be null");
        this.status = SimulationRunStatus.COMPLETED;
    }

    public void fail(int publishedEventCount, Instant completedAt) {
        this.publishedEventCount = publishedEventCount;
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt must not be null");
        this.status = SimulationRunStatus.FAILED;
    }

    public UUID runId() {
        return runId;
    }

    public String scenarioId() {
        return scenarioId;
    }

    public String characterId() {
        return characterId;
    }

    public UUID correlationId() {
        return correlationId;
    }

    public int requestedEventCount() {
        return requestedEventCount;
    }

    public int publishedEventCount() {
        return publishedEventCount;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public SimulationRunStatus status() {
        return status;
    }
}
