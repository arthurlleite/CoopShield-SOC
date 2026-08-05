package com.coopshield.soc.accesscontrol.infrastructure;

import com.coopshield.soc.audit.application.AuditPort;
import com.coopshield.soc.audit.domain.AuditEventType;
import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.coopshield.soc.sharedkernel.identity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RestAccessDeniedHandlerTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void writesGenericForbiddenBodyAndRecordsAudit() throws Exception {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(UUID.randomUUID(), "synthetic-employee-01", Role.EMPLOYEE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of()));

        AuditPort auditPort = mock(AuditPort.class);
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler(new ObjectMapper(), auditPort);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/whatever");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("irrelevant"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"access_denied\"");
        verify(auditPort).record(argThat(event ->
                event.eventType() == AuditEventType.AUTHORIZATION_DENIED
                        && event.actor().equals("synthetic-employee-01")
                        && "/api/v1/admin/whatever".equals(event.details().get("path"))));
    }

    @Test
    void recordsUnknownActorWhenNoAuthenticationPresent() throws Exception {
        AuditPort auditPort = mock(AuditPort.class);
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler(new ObjectMapper(), auditPort);

        handler.handle(new MockHttpServletRequest(), new MockHttpServletResponse(), new AccessDeniedException("irrelevant"));

        verify(auditPort).record(argThat(event -> event.actor().equals("unknown")));
    }
}
