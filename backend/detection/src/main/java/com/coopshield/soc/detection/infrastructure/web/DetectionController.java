package com.coopshield.soc.detection.infrastructure.web;

import com.coopshield.soc.detection.application.DetectionMatchRepository;
import com.coopshield.soc.detection.application.DetectionRuleRepository;
import com.coopshield.soc.detection.domain.DetectionMatch;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Leitura das regras carregadas e das correspondencias detectadas -
 * satisfaz UC-18 (visualizacao de regras versionadas) com a implementacao
 * completa de gerenciamento de regras (habilitar/desabilitar via API)
 * chegando na Fase 10, junto ao frontend Detection Rules.
 */
@RestController
@RequestMapping("/api/v1/detection")
public class DetectionController {

    private final DetectionRuleRepository ruleRepository;
    private final DetectionMatchRepository matchRepository;

    public DetectionController(DetectionRuleRepository ruleRepository, DetectionMatchRepository matchRepository) {
        this.ruleRepository = ruleRepository;
        this.matchRepository = matchRepository;
    }

    @GetMapping("/rules")
    public List<DetectionRuleResponse> rules() {
        return ruleRepository.findAll().stream().map(DetectionRuleResponse::from).toList();
    }

    @GetMapping("/matches")
    public List<DetectionMatchResponse> matches(@RequestParam(required = false) String correlationId) {
        List<DetectionMatch> matches = correlationId == null
                ? matchRepository.findAll()
                : matchRepository.findByCorrelationId(correlationId);
        return matches.stream().map(DetectionMatchResponse::from).toList();
    }

    @GetMapping("/matches/{matchId}")
    public DetectionMatchResponse match(@PathVariable String matchId) {
        return matchRepository.findById(UUID.fromString(matchId))
                .map(DetectionMatchResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Unknown match: " + matchId));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
