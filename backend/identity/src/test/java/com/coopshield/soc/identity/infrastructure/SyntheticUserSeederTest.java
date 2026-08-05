package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.domain.User;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticUserSeederTest {

    @Test
    void seedsOneEnabledUserPerRoleWithTheSyntheticPassword() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
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
}
