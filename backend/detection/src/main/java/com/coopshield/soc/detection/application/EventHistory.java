package com.coopshield.soc.detection.application;

import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Janela deslizante de eventos recentes por ator e por dispositivo, usada
 * pelos avaliadores de regra que precisam de agregacao (contagem, sequencia,
 * viagem impossivel). Ver ADR-014 sobre a limitacao conhecida da
 * implementacao em memoria (nao distribuida, perdida a reinicio do
 * processo) - aceitavel para este MVP educacional de instancia unica.
 */
public interface EventHistory {

    void record(EventEnvelope event);

    List<EventEnvelope> byActor(String actorUserId, Duration window, Instant now);

    List<EventEnvelope> byDevice(String deviceId, Duration window, Instant now);
}
