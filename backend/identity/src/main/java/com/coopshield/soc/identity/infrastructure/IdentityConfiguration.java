package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.audit.application.AuditPort;
import com.coopshield.soc.identity.application.AccessTokenIssuer;
import com.coopshield.soc.identity.application.AuthenticationService;
import com.coopshield.soc.identity.application.RefreshTokenRepository;
import com.coopshield.soc.identity.application.UserRepository;
import com.coopshield.soc.identity.domain.AccountLockoutPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Composicao dos beans do modulo identity: liga as portas de aplicacao aos
 * adaptadores de infraestrutura. Mantida na propria infraestrutura do
 * modulo para que {@code AuthenticationService} (camada de aplicacao)
 * permaneca livre de anotacoes do Spring.
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, LockoutProperties.class})
public class IdentityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccountLockoutPolicy accountLockoutPolicy(LockoutProperties properties) {
        return new AccountLockoutPolicy(properties.getMaxFailedAttempts(), properties.getLockoutDuration());
    }

    @Bean
    public AuthenticationService authenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AccessTokenIssuer accessTokenIssuer,
            PasswordEncoder passwordEncoder,
            AccountLockoutPolicy accountLockoutPolicy,
            AuditPort auditPort,
            LockoutProperties lockoutProperties
    ) {
        return new AuthenticationService(
                userRepository,
                refreshTokenRepository,
                accessTokenIssuer,
                passwordEncoder,
                accountLockoutPolicy,
                auditPort,
                lockoutProperties.getRefreshTokenTtl());
    }
}
