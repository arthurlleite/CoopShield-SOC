package com.coopshield.soc.audit.domain;

/**
 * Tipos de evento de auditoria registrados pelo CoopShield SOC. A lista
 * cresce conforme novas fases adicionam acoes sensiveis a auditar (ex.:
 * destokenizacao, na Fase 9).
 */
public enum AuditEventType {
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAILURE,
    ACCOUNT_LOCKED,
    TOKEN_REFRESHED,
    TOKEN_REFRESH_DENIED,
    LOGOUT,
    AUTHORIZATION_DENIED
}
