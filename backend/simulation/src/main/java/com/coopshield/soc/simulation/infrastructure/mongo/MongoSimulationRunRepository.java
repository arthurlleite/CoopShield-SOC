package com.coopshield.soc.simulation.infrastructure.mongo;

import com.coopshield.soc.simulation.application.SimulationRunRepository;
import com.coopshield.soc.simulation.domain.SimulationRun;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class MongoSimulationRunRepository implements SimulationRunRepository {

    private final SpringDataSimulationRunMongoRepository springDataRepository;

    public MongoSimulationRunRepository(SpringDataSimulationRunMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(SimulationRun run) {
        springDataRepository.save(SimulationRunDocument.fromDomain(run));
    }

    @Override
    public Optional<SimulationRun> findById(UUID runId) {
        return springDataRepository.findById(runId.toString()).map(SimulationRunDocument::toDomain);
    }
}
