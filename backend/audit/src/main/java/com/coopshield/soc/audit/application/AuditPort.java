package com.coopshield.soc.audit.application;

import com.coopshield.soc.audit.domain.AuditEvent;

/**
 * Porta comum utilizada pelos demais modulos para registrar eventos de
 * auditoria, conforme docs/architecture/overview.md ("audit recebe
 * eventos de auditoria dos demais modulos via uma porta comum").
 */
public interface AuditPort {

    void record(AuditEvent event);
}
