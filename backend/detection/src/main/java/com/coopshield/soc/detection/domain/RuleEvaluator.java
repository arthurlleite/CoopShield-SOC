package com.coopshield.soc.detection.domain;

import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.util.List;
import java.util.Optional;

/**
 * Avalia uma regra contra o evento normalizado recebido, usando o historico
 * recente relevante (ja filtrado por janela e chave de agregacao - ator ou
 * dispositivo, ver {@code conditions.aggregationKey} - pelo chamador). Puro
 * e sem dependencia de infraestrutura: nenhuma consulta a banco/Kafka
 * acontece aqui, apenas logica de negocio sobre dados ja fornecidos.
 */
public interface RuleEvaluator {

    String type();

    Optional<DetectionMatch> evaluate(DetectionRule rule, EventEnvelope event, List<EventEnvelope> recentHistory);
}
