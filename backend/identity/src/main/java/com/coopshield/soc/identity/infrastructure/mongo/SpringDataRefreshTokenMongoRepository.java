package com.coopshield.soc.identity.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataRefreshTokenMongoRepository extends MongoRepository<RefreshTokenDocument, String> {
}
