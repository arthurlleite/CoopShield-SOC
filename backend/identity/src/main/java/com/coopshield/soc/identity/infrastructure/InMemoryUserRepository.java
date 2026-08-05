package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.application.UserRepository;
import com.coopshield.soc.identity.domain.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador em memoria de {@link UserRepository}. Sera substituido por um
 * adaptador MongoDB na Fase 3, sem alterar a porta nem os consumidores
 * (ver docs/adr/ADR-011-persistencia-em-memoria-fase-2.md).
 */
@Component
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byUsername = new ConcurrentHashMap<>();
    private final Map<UUID, User> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(byId.get(userId));
    }

    @Override
    public void save(User user) {
        Objects.requireNonNull(user, "user must not be null");
        byUsername.put(user.username(), user);
        byId.put(user.userId(), user);
    }
}
