package com.coopshield.soc.simulation.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataSimulationRunMongoRepository extends MongoRepository<SimulationRunDocument, String> {
}
