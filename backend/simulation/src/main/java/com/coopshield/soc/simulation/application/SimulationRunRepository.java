package com.coopshield.soc.simulation.application;

import com.coopshield.soc.simulation.domain.SimulationRun;

import java.util.Optional;
import java.util.UUID;

public interface SimulationRunRepository {

    void save(SimulationRun run);

    Optional<SimulationRun> findById(UUID runId);
}
