package com.coopshield.soc.accesscontrol.infrastructure;

import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.coopshield.soc.sharedkernel.identity.TokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autentica requisicoes a partir do header {@code Authorization: Bearer
 * <token>}, delegando a validacao do token para {@link TokenValidator}
 * (implementado pelo modulo identity). Nao conhece o formato do token
 * (JWT ou outro) - apenas o contrato compartilhado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenValidator tokenValidator;

    public JwtAuthenticationFilter(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String rawToken = header.substring(BEARER_PREFIX.length());
            tokenValidator.validateAccessToken(rawToken).ifPresent(principal -> authenticate(principal, request));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(AuthenticatedPrincipal principal, HttpServletRequest request) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
