package com.coopshield.soc.eventingestion.domain;

import java.util.List;

/**
 * Lancada quando um evento bruto recebido nao atende aos campos
 * obrigatorios de ingestao (ver docs/event-catalog/events.md).
 */
public class RawEventValidationException extends RuntimeException {

    private final List<String> violations;

    public RawEventValidationException(List<String> violations) {
        super("Raw event failed validation: " + String.join(", ", violations));
        this.violations = List.copyOf(violations);
    }

    public List<String> violations() {
        return violations;
    }
}
