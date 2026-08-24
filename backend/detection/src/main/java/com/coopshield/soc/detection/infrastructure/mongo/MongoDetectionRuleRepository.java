package com.coopshield.soc.detection.infrastructure.mongo;

import com.coopshield.soc.detection.application.DetectionRuleRepository;
import com.coopshield.soc.detection.domain.DetectionRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MongoDetectionRuleRepository implements DetectionRuleRepository {

    private final SpringDataDetectionRuleMongoRepository springDataRepository;

    public MongoDetectionRuleRepository(SpringDataDetectionRuleMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void saveAll(List<DetectionRule> rules) {
        springDataRepository.saveAll(rules.stream().map(DetectionRuleDocument::fromDomain).toList());
    }

    @Override
    public List<DetectionRule> findAll() {
        return springDataRepository.findAll().stream().map(DetectionRuleDocument::toDomain).toList();
    }

    @Override
    public Optional<DetectionRule> findById(String ruleId) {
        return springDataRepository.findById(ruleId).map(DetectionRuleDocument::toDomain);
    }
}
