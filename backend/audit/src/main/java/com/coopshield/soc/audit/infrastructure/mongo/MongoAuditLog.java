package com.coopshield.soc.audit.infrastructure.mongo;

import com.coopshield.soc.audit.application.AuditPort;
import com.coopshield.soc.audit.domain.AuditEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador MongoDB de {@link AuditPort} (colecao {@code audit_logs}).
 * Substitui o adaptador em memoria da Fase 2 (ver
 * docs/adr/ADR-012-mongodb-real-fase-3.md).
 */
@Component
public class MongoAuditLog implements AuditPort {

    private final SpringDataAuditEventMongoRepository springDataRepository;

    public MongoAuditLog(SpringDataAuditEventMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void record(AuditEvent event) {
        springDataRepository.save(AuditEventDocument.fromDomain(event));
    }

    /**
     * Exposto para fins de teste e inspecao local; a consulta de auditoria
     * pela interface (Audit Explorer) chega na Fase 9, com filtros por
     * ator, acao e periodo.
     */
    public List<AuditEvent> findAll() {
        return springDataRepository.findAll().stream().map(AuditEventDocument::toDomain).toList();
    }
}
