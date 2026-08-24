package com.coopshield.soc.audit.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataAuditEventMongoRepository extends MongoRepository<AuditEventDocument, String> {
}
