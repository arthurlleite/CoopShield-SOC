package com.coopshield.soc.detection.infrastructure.history;

import com.coopshield.soc.detection.application.EventHistory;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Janela deslizante em memoria, por ator e por dispositivo. Limitacao
 * conhecida e documentada (ver ADR-014): estado de uma unica instancia,
 * perdido a reinicio do processo - aceitavel para o MVP educacional de
 * instancia unica; um deploy com multiplas instancias ou um armazenamento
 * distribuido (ex.: Kafka Streams state store, Redis) seria necessario para
 * produzir de verdade.
 */
@Component
public class InMemoryEventHistory implements EventHistory {

    private static final Duration MAX_RETENTION = Duration.ofMinutes(30);

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<EventEnvelope>> byActor = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<EventEnvelope>> byDevice = new ConcurrentHashMap<>();

    @Override
    public void record(EventEnvelope event) {
        append(byActor, event.actor().userId(), event);
        append(byDevice, event.device().deviceId(), event);
    }

    @Override
    public List<EventEnvelope> byActor(String actorUserId, Duration window, Instant now) {
        return recent(byActor, actorUserId, window, now);
    }

    @Override
    public List<EventEnvelope> byDevice(String deviceId, Duration window, Instant now) {
        return recent(byDevice, deviceId, window, now);
    }

    private void append(ConcurrentHashMap<String, CopyOnWriteArrayList<EventEnvelope>> store, String key, EventEnvelope event) {
        CopyOnWriteArrayList<EventEnvelope> events = store.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        events.add(event);
        Instant cutoff = event.timestamp().minus(MAX_RETENTION);
        events.removeIf(e -> e.timestamp().isBefore(cutoff));
    }

    private List<EventEnvelope> recent(ConcurrentHashMap<String, CopyOnWriteArrayList<EventEnvelope>> store, String key,
                                        Duration window, Instant now) {
        CopyOnWriteArrayList<EventEnvelope> events = store.get(key);
        if (events == null) {
            return List.of();
        }
        Instant cutoff = now.minus(window);
        List<EventEnvelope> result = new ArrayList<>();
        for (EventEnvelope event : events) {
            // "now" e o timestamp do proprio evento sendo avaliado, ainda nao
            // registrado neste ponto (DetectionEngine so chama record() depois
            // de avaliar) - eventos anteriores com o MESMO timestamp (mesmo
            // milissegundo, comum em rajadas rapidas, sinteticas ou reais) sao
            // legitimamente "ja aconteceram" e devem contar, por isso o limite
            // superior e inclusivo (!isAfter), nao estritamente anterior.
            if (!event.timestamp().isBefore(cutoff) && !event.timestamp().isAfter(now)) {
                result.add(event);
            }
        }
        return result;
    }
}
