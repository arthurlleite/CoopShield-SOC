package com.coopshield.soc.simulation.infrastructure.web;

import com.coopshield.soc.simulation.application.SimulationRunRepository;
import com.coopshield.soc.simulation.application.SimulationService;
import com.coopshield.soc.simulation.domain.InvalidEventCountException;
import com.coopshield.soc.simulation.domain.SimulationRun;
import com.coopshield.soc.simulation.domain.UnknownCharacterException;
import com.coopshield.soc.simulation.domain.UnknownScenarioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Laboratorio de simulacao: lista personagens/cenarios disponiveis e
 * executa um cenario para um personagem, publicando os eventos gerados
 * atraves do mesmo caminho de {@code POST /api/v1/events} (ver
 * {@link SimulationService}).
 */
@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final SimulationService simulationService;
    private final SimulationRunRepository runRepository;

    public SimulationController(SimulationService simulationService, SimulationRunRepository runRepository) {
        this.simulationService = simulationService;
        this.runRepository = runRepository;
    }

    @GetMapping("/scenarios")
    public List<ScenarioResponse> scenarios() {
        return simulationService.scenarios().stream().map(ScenarioResponse::from).toList();
    }

    @GetMapping("/characters")
    public List<CharacterResponse> characters() {
        return simulationService.characters().stream().map(CharacterResponse::from).toList();
    }

    @PostMapping("/runs")
    public ResponseEntity<RunResponse> startRun(@Valid @RequestBody StartRunRequest request) {
        SimulationRun run = simulationService.start(request.scenarioId(), request.characterId(), request.eventCount());
        return ResponseEntity.accepted().body(RunResponse.from(run));
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<RunResponse> getRun(@PathVariable String runId) {
        return runRepository.findById(UUID.fromString(runId))
                .map(run -> ResponseEntity.ok(RunResponse.from(run)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler({UnknownScenarioException.class, UnknownCharacterException.class, InvalidEventCountException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("invalid_simulation_request", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleInvalidRunId() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("invalid_run_id", "runId must be a valid UUID."));
    }
}
