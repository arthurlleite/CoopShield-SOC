package com.coopshield.soc.identity.application;

import com.coopshield.soc.identity.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida para persistencia de usuarios. Implementada em memoria
 * na Fase 2; a Fase 3 introduz um adaptador MongoDB implementando a mesma
 * porta, sem alterar o dominio ou a camada de aplicacao (ver
 * docs/adr/ADR-011-persistencia-em-memoria-fase-2.md).
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID userId);

    void save(User user);
}
