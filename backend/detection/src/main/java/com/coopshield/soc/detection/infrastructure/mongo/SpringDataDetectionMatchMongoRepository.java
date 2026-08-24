package com.coopshield.soc.detection.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

interface SpringDataDetectionMatchMongoRepository extends MongoRepository<DetectionMatchDocument, String> {

    List<DetectionMatchDocument> findByCorrelationId(String correlationId);
}
