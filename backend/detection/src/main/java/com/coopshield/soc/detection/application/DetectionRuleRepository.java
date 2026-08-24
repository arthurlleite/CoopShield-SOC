package com.coopshield.soc.detection.application;

import com.coopshield.soc.detection.domain.DetectionRule;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saida para o espelho consultavel (colecao {@code detection_rules})
 * das regras carregadas de YAML na inicializacao. A fonte da verdade
 * permanece o YAML versionado (ver ADR-006); este espelho existe para
 * consulta futura (UC-18, Fase 10).
 */
public interface DetectionRuleRepository {

    void saveAll(List<DetectionRule> rules);

    List<DetectionRule> findAll();

    Optional<DetectionRule> findById(String ruleId);
}
