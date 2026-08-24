package com.coopshield.soc.eventnormalization.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataSecurityEventMongoRepository extends MongoRepository<SecurityEventDocument, String> {
}
