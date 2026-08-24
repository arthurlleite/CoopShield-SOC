package com.coopshield.soc.detection.domain;

/**
 * Lancada na inicializacao quando um arquivo YAML em {@code detection-rules/}
 * nao atende ao schema esperado (ver ADR-006) ou referencia um
 * {@code evaluatorType} desconhecido.
 */
public class InvalidDetectionRuleException extends RuntimeException {

    public InvalidDetectionRuleException(String resourceName, String reason) {
        super("Invalid detection rule in " + resourceName + ": " + reason);
    }
}
