package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.domain.User;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryUserRepositoryTest {

    @Test
    void savesAndFindsByUsernameAndById() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        User user = new User(UUID.randomUUID(), "synthetic-analyst-01", "hash", Role.SOC_ANALYST, true);

        repository.save(user);

        assertThat(repository.findByUsername("synthetic-analyst-01")).contains(user);
        assertThat(repository.findById(user.userId())).contains(user);
    }

    @Test
    void returnsEmptyForUnknownUser() {
        InMemoryUserRepository repository = new InMemoryUserRepository();

        assertThat(repository.findByUsername("ghost")).isEmpty();
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
