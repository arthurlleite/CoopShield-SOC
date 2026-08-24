package com.coopshield.soc.simulation.infrastructure.mongo;

import com.coopshield.soc.simulation.domain.SimulationRun;
import com.coopshield.soc.simulation.domain.SimulationRunStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de persistencia da colecao {@code simulation_runs} - reservada
 * para a Fase 5 em docs/adr/ADR-012-mongodb-real-fase-3.md.
 */
@Document(collection = "simulation_runs")
public class SimulationRunDocument {

    @Id
    private String runId;

    @Indexed
    private String scenarioId;

    private String characterId;
    private String correlationId;
    private int requestedEventCount;
    private int publishedEventCount;

    @Indexed
    private Instant startedAt;

    private Instant completedAt;
    private SimulationRunStatus status;

    protected SimulationRunDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public SimulationRunDocument(String runId, String scenarioId, String characterId, String correlationId,
                                  int requestedEventCount, int publishedEventCount, Instant startedAt,
                                  Instant completedAt, SimulationRunStatus status) {
        this.runId = runId;
        this.scenarioId = scenarioId;
        this.characterId = characterId;
        this.correlationId = correlationId;
        this.requestedEventCount = requestedEventCount;
        this.publishedEventCount = publishedEventCount;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
    }

    public static SimulationRunDocument fromDomain(SimulationRun run) {
        return new SimulationRunDocument(
                run.runId().toString(), run.scenarioId(), run.characterId(), run.correlationId().toString(),
                run.requestedEventCount(), run.publishedEventCount(), run.startedAt(), run.completedAt(), run.status());
    }

    public SimulationRun toDomain() {
        return SimulationRun.rehydrate(
                UUID.fromString(runId), scenarioId, characterId, UUID.fromString(correlationId),
                requestedEventCount, publishedEventCount, startedAt, completedAt, status);
    }
}
