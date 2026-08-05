package com.coopshield.soc.accesscontrol.infrastructure;

import com.coopshield.soc.audit.application.AuditPort;
import com.coopshield.soc.audit.domain.AuditEvent;
import com.coopshield.soc.audit.domain.AuditEventType;
import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Resposta padrao para requisicoes autenticadas mas sem o perfil
 * necessario: JSON generico e registro de auditoria da tentativa negada
 * (RF-19 - registro de falhas de autorizacao).
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final ApiErrorBody BODY = new ApiErrorBody("access_denied", "Acesso negado para o perfil atual.");

    private final ObjectMapper objectMapper;
    private final AuditPort auditPort;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, AuditPort auditPort) {
        this.objectMapper = objectMapper;
        this.auditPort = auditPort;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        auditPort.record(AuditEvent.of(AuditEventType.AUTHORIZATION_DENIED, currentActor(), Map.of("path", request.getRequestURI())));

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), BODY);
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            return principal.username();
        }
        return "unknown";
    }
}
