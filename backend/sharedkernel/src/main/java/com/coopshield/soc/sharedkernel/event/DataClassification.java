package com.coopshield.soc.sharedkernel.event;

/**
 * Classificacao de sensibilidade de um evento, atribuida pelo modulo
 * dataprotection antes de qualquer persistencia ou publicacao para analise.
 */
public enum DataClassification {
    PUBLIC,
    INTERNAL,
    SENSITIVE,
    RESTRICTED
}
