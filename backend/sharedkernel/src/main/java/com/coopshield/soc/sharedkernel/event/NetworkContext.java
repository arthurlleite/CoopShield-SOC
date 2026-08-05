package com.coopshield.soc.sharedkernel.event;

import java.util.Objects;

/**
 * Contexto de rede (sintetico/anonimizado) associado ao evento.
 *
 * @param ipHash hash do endereco IP de origem (nunca o IP em texto puro)
 * @param geo    regiao geografica sintetica aproximada
 */
public record NetworkContext(String ipHash, String geo) {

    public NetworkContext {
        Objects.requireNonNull(ipHash, "ipHash must not be null");
        Objects.requireNonNull(geo, "geo must not be null");
        if (ipHash.isBlank()) {
            throw new IllegalArgumentException("ipHash must not be blank");
        }
    }
}
