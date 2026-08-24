package com.coopshield.soc.identity.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface SpringDataUserMongoRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByUsername(String username);
}
