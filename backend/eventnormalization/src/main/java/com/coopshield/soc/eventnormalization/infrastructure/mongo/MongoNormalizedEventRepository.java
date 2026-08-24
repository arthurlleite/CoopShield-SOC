package com.coopshield.soc.eventnormalization.infrastructure.mongo;

import com.coopshield.soc.eventnormalization.application.NormalizedEventRepository;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.identifiers.EventId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador MongoDB de {@link NormalizedEventRepository} (colecao
 * {@code security_events}). Usa {@code insert} (nao {@code save}) porque a
 * idempotencia depende de uma tentativa de insercao duplicada falhar - um
 * {@code save} faria upsert silencioso e mascararia o reprocessamento.
 */
@Component
public class MongoNormalizedEventRepository implements NormalizedEventRepository {

    private final SpringDataSecurityEventMongoRepository springDataRepository;

    public MongoNormalizedEventRepository(SpringDataSecurityEventMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public boolean saveIfAbsent(EventEnvelope event) {
        try {
            springDataRepository.insert(SecurityEventDocument.fromDomain(event));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    @Override
    public Optional<EventEnvelope> findByEventId(EventId eventId) {
        return springDataRepository.findById(eventId.toString()).map(SecurityEventDocument::toDomain);
    }
}
