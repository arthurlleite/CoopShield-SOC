package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.application.UserRepository;
import com.coopshield.soc.identity.domain.User;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticUserSeederTest {

    @Test
    void seedsOneEnabledUserPerRoleWithTheSyntheticPassword() {
        FakeUserRepository repository = new FakeUserRepository();
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        SyntheticUserSeeder seeder = new SyntheticUserSeeder(repository, encoder);

        seeder.run(new DefaultApplicationArguments());

        for (Role role : Role.values()) {
            String username = "synthetic-" + role.name().toLowerCase().replace('_', '-') + "-01";
            User user = repository.findByUsername(username).orElseThrow();

            assertThat(user.role()).isEqualTo(role);
            assertThat(user.enabled()).isTrue();
            assertThat(encoder.matches(SyntheticUserSeeder.SYNTHETIC_PASSWORD, user.passwordHash())).isTrue();
        }
    }

    /**
     * Duplo de teste minimo (nao a antiga classe de producao em memoria,
     * removida na Fase 3 em favor do adaptador MongoDB): usado aqui apenas
     * para observar o que o seeder salva, sem depender de um MongoDB real
     * para um teste que nao exercita persistencia de verdade.
     */
    private static final class FakeUserRepository implements UserRepository {
        private final Map<String, User> byUsername = new HashMap<>();

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(byUsername.get(username));
        }

        @Override
        public Optional<User> findById(UUID userId) {
            return byUsername.values().stream().filter(u -> u.userId().equals(userId)).findFirst();
        }

        @Override
        public void save(User user) {
            byUsername.put(user.username(), user);
        }
    }
}
