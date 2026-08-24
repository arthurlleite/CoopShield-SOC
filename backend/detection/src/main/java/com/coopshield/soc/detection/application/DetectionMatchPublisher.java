package com.coopshield.soc.detection.application;

import com.coopshield.soc.detection.domain.DetectionMatch;

/**
 * Porta de saida para publicacao de correspondencias de regra no topico
 * {@code security.detection-alerts}. O motor de risco (Fase 7) e o modulo
 * de alerta (Fase 8) consumirao este topico para calcular a pontuacao final
 * e criar o alerta persistente, respectivamente - ver ADR-014.
 */
public interface DetectionMatchPublisher {

    void publish(DetectionMatch match);
}
