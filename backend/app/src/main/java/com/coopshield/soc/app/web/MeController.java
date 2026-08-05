package com.coopshield.soc.app.web;

import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoint minimo, real e generico para qualquer usuario autenticado
 * consultar a propria identidade - util tanto como exemplo de rota
 * protegida quanto para o frontend confirmar a sessao apos o login.
 */
@RestController
public class MeController {

    @GetMapping("/api/v1/me")
    public MeResponse me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return new MeResponse(principal.userId(), principal.username(), principal.role());
    }

    public record MeResponse(UUID userId, String username, Role role) {
    }
}
