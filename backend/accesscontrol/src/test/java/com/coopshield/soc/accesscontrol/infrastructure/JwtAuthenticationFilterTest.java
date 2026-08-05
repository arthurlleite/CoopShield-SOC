package com.coopshield.soc.accesscontrol.infrastructure;

import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.coopshield.soc.sharedkernel.identity.Role;
import com.coopshield.soc.sharedkernel.identity.TokenValidator;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWithValidBearerToken() throws Exception {
        TokenValidator validator = mock(TokenValidator.class);
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(UUID.randomUUID(), "synthetic-analyst-01", Role.SOC_ANALYST);
        when(validator.validateAccessToken("valid-token")).thenReturn(Optional.of(principal));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(principal);
        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_SOC_ANALYST");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doesNotAuthenticateWhenTokenIsInvalid() throws Exception {
        TokenValidator validator = mock(TokenValidator.class);
        when(validator.validateAccessToken("bad-token")).thenReturn(Optional.empty());

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doesNotTouchValidatorWhenNoAuthorizationHeaderPresent() throws Exception {
        TokenValidator validator = mock(TokenValidator.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verifyNoInteractions(validator);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
