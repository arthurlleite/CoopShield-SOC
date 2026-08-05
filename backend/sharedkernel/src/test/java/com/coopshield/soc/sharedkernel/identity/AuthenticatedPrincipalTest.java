package com.coopshield.soc.sharedkernel.identity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedPrincipalTest {

    @Test
    void createsValidPrincipal() {
        UUID userId = UUID.randomUUID();

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(userId, "synthetic-analyst-01", Role.SOC_ANALYST);

        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.username()).isEqualTo("synthetic-analyst-01");
        assertThat(principal.role()).isEqualTo(Role.SOC_ANALYST);
    }

    @Test
    void rejectsNullRole() {
        assertThatThrownBy(() -> new AuthenticatedPrincipal(UUID.randomUUID(), "synthetic-analyst-01", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("role");
    }
}
