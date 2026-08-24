package com.coopshield.soc.detection.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataDetectionRuleMongoRepository extends MongoRepository<DetectionRuleDocument, String> {
}
