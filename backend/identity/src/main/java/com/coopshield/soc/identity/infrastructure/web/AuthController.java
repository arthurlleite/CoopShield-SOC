package com.coopshield.soc.identity.infrastructure.web;

import com.coopshield.soc.identity.application.AuthenticationResult;
import com.coopshield.soc.identity.application.AuthenticationService;
import com.coopshield.soc.identity.domain.AccountLockedException;
import com.coopshield.soc.identity.domain.InvalidCredentialsException;
import com.coopshield.soc.identity.domain.InvalidRefreshTokenException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticacao. As excecoes de credenciais invalidas e de
 * conta bloqueada sao deliberadamente mapeadas para a MESMA resposta
 * generica, para nao permitir enumeracao de usuarios nem revelar o estado
 * de bloqueio a um observador externo (ver
 * {@link com.coopshield.soc.identity.domain.AccountLockedException}).
 */
@RestController
public class AuthController {

    private static final ApiError INVALID_CREDENTIALS_ERROR =
            new ApiError("invalid_credentials", "Usuario ou senha invalidos.");
    private static final ApiError INVALID_REFRESH_TOKEN_ERROR =
            new ApiError("invalid_refresh_token", "Refresh token invalido ou expirado.");

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = authenticationService.login(request.username(), request.password());
        return ResponseEntity.ok(TokenResponse.from(result));
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthenticationResult result = authenticationService.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenResponse.from(result));
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({InvalidCredentialsException.class, AccountLockedException.class})
    public ResponseEntity<ApiError> handleAuthenticationFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_CREDENTIALS_ERROR);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_REFRESH_TOKEN_ERROR);
    }
}
