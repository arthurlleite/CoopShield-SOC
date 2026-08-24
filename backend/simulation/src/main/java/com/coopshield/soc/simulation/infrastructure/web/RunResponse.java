package com.coopshield.soc.simulation.infrastructure.web;

import com.coopshield.soc.simulation.domain.SimulationRun;

import java.time.Instant;

public record RunResponse(
        String runId,
        String scenarioId,
        String characterId,
        String correlationId,
        int requestedEventCount,
        int publishedEventCount,
        String status,
        Instant startedAt,
        Instant completedAt
) {

    public static RunResponse from(SimulationRun run) {
        return new RunResponse(
                run.runId().toString(), run.scenarioId(), run.characterId(), run.correlationId().toString(),
                run.requestedEventCount(), run.publishedEventCount(), run.status().name(),
                run.startedAt(), run.completedAt());
    }
}
