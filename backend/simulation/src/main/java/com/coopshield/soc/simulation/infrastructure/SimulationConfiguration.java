package com.coopshield.soc.simulation.infrastructure;

import com.coopshield.soc.eventingestion.application.EventIngestionService;
import com.coopshield.soc.simulation.application.SimulationRunRepository;
import com.coopshield.soc.simulation.application.SimulationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composicao dos beans do modulo simulation: liga a porta de aplicacao ao
 * adaptador MongoDB e reaproveita o {@link EventIngestionService} ja
 * publicado pelo modulo eventingestion para efetivamente gerar trafego no
 * pipeline (ver ADR-013).
 */
@Configuration
public class SimulationConfiguration {

    @Bean
    public SimulationService simulationService(EventIngestionService ingestionService, SimulationRunRepository repository) {
        return new SimulationService(ingestionService, repository);
    }
}
