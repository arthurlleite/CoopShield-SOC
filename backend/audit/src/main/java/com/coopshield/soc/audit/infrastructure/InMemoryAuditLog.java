package com.coopshield.soc.audit.infrastructure;

import com.coopshield.soc.audit.application.AuditPort;
import com.coopshield.soc.audit.domain.AuditEvent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptador de auditoria em memoria. Suficiente para a Fase 2, onde ainda
 * nao ha persistencia em MongoDB (introduzida na Fase 3); os eventos
 * registrados aqui nao sobrevivem a um reinicio da aplicacao.
 */
@Component
public class InMemoryAuditLog implements AuditPort {

    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void record(AuditEvent event) {
        events.add(event);
    }

    /**
     * Exposto para fins de teste e inspecao local; a consulta de auditoria
     * pela interface (Audit Explorer) chega na Fase 9, com persistencia
     * real em MongoDB.
     */
    public List<AuditEvent> findAll() {
        return Collections.unmodifiableList(events);
    }
}
