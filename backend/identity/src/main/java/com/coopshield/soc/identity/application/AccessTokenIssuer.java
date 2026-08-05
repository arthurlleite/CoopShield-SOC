package com.coopshield.soc.identity.application;

import com.coopshield.soc.sharedkernel.identity.Role;

import java.util.UUID;

/**
 * Porta de emissao de access tokens (JWT), implementada pela
 * infraestrutura do modulo identity. Mantida separada de
 * {@link com.coopshield.soc.sharedkernel.identity.TokenValidator} porque
 * apenas o proprio modulo identity emite tokens; outros modulos
 * (accesscontrol) somente os validam.
 */
public interface AccessTokenIssuer {

    AccessToken issue(UUID userId, String username, Role role);
}
