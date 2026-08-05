package com.coopshield.soc.app.security;

import com.coopshield.soc.accesscontrol.infrastructure.JwtAuthenticationFilter;
import com.coopshield.soc.accesscontrol.infrastructure.RestAccessDeniedHandler;
import com.coopshield.soc.accesscontrol.infrastructure.RestAuthenticationEntryPoint;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cadeia de filtros de seguranca do CoopShield SOC: API stateless
 * autenticada por JWT (sem sessao, sem CSRF, sem login por formulario),
 * com regras de autorizacao por prefixo de rota mapeadas aos perfis
 * definidos em docs/architecture/roles-permissions.md.
 *
 * <p>Os prefixos {@code /api/v1/admin}, {@code /api/v1/audit} e
 * {@code /api/v1/soc} ainda nao possuem controladores de negocio (estes
 * chegam nas fases 6 a 9); a regra de autorizacao ja fica em vigor desde
 * agora e e validada nos testes de integracao pela distincao entre 403
 * (bloqueado pela regra de autorizacao) e 404 (rota liberada, mas sem
 * endpoint ainda).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health", "/actuator/health/**", "/actuator/info",
                                "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/api/v1/auth/**", "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole(Role.IT_ADMIN.name())
                        .requestMatchers("/api/v1/audit/**").hasRole(Role.AUDITOR.name())
                        .requestMatchers("/api/v1/soc/**").hasAnyRole(Role.SOC_ANALYST.name(), Role.SOC_MANAGER.name())
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
