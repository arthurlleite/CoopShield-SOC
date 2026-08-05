package com.coopshield.soc.sharedkernel.event;

import java.util.Objects;

/**
 * Dispositivo (sintetico) de origem do evento.
 *
 * @param deviceId identificador sintetico do dispositivo
 * @param known    se o dispositivo e reconhecido na linha de base do ator
 */
public record DeviceContext(String deviceId, boolean known) {

    public DeviceContext {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        if (deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId must not be blank");
        }
    }
}
