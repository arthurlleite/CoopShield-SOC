package com.coopshield.soc.detection.infrastructure.mongo;

import com.coopshield.soc.detection.application.DetectionMatchRepository;
import com.coopshield.soc.detection.domain.DetectionMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MongoDetectionMatchRepository implements DetectionMatchRepository {

    private final SpringDataDetectionMatchMongoRepository springDataRepository;

    public MongoDetectionMatchRepository(SpringDataDetectionMatchMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(DetectionMatch match) {
        springDataRepository.save(DetectionMatchDocument.fromDomain(match));
    }

    @Override
    public List<DetectionMatch> findByCorrelationId(String correlationId) {
        return springDataRepository.findByCorrelationId(correlationId).stream()
                .map(DetectionMatchDocument::toDomain).toList();
    }

    @Override
    public List<DetectionMatch> findAll() {
        return springDataRepository.findAll().stream().map(DetectionMatchDocument::toDomain).toList();
    }

    @Override
    public Optional<DetectionMatch> findById(UUID matchId) {
        return springDataRepository.findById(matchId.toString()).map(DetectionMatchDocument::toDomain);
    }
}
