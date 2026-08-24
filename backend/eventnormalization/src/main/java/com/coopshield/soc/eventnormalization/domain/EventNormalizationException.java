package com.coopshield.soc.eventnormalization.domain;

import java.util.List;

/**
 * Lancada quando uma mensagem recebida em {@code security.raw-events} nao
 * pode ser normalizada em um {@link com.coopshield.soc.sharedkernel.event.EventEnvelope}
 * valido - JSON malformado ou campo obrigatorio ausente/invalido. Sempre
 * tratada como falha permanente (nao-transiente): o listener a encaminha
 * para {@code security.dead-letter} em vez de tentar novamente, ja que
 * reprocessar a mesma mensagem produziria o mesmo erro (ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md).
 */
public class EventNormalizationException extends RuntimeException {

    private final List<String> violations;

    public EventNormalizationException(List<String> violations) {
        super("Raw event failed normalization: " + String.join(", ", violations));
        this.violations = List.copyOf(violations);
    }

    public EventNormalizationException(String reason, Throwable cause) {
        super("Raw event failed normalization: " + reason, cause);
        this.violations = List.of(reason);
    }

    public List<String> violations() {
        return violations;
    }
}
