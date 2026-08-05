package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.application.UserRepository;
import com.coopshield.soc.identity.domain.PasswordPolicy;
import com.coopshield.soc.identity.domain.User;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Cria um usuario sintetico por perfil na inicializacao, para permitir
 * login local/demonstrativo sem depender de um banco de dados (a
 * persistencia real chega na Fase 3). Nenhum destes usuarios ou senhas
 * representa uma credencial real; a senha compartilhada e documentada em
 * backend/README.md, nunca impressa em log.
 */
@Component
public class SyntheticUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SyntheticUserSeeder.class);

    /**
     * Senha sintetica unica para todos os usuarios de demonstracao desta
     * fase. Documentada em backend/README.md; nunca deve ser registrada em
     * log, mesmo sendo sintetica.
     */
    static final String SYNTHETIC_PASSWORD = "Synthetic#Pass123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SyntheticUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        PasswordPolicy.validate(SYNTHETIC_PASSWORD);
        String hash = passwordEncoder.encode(SYNTHETIC_PASSWORD);

        for (Role role : Role.values()) {
            String username = "synthetic-" + role.name().toLowerCase().replace('_', '-') + "-01";
            userRepository.save(new User(UUID.randomUUID(), username, hash, role, true));
        }

        log.info("Seeded {} synthetic users (one per role) for local/demo login.", Role.values().length);
    }
}
