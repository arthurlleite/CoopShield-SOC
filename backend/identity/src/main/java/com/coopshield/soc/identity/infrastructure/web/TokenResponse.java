package com.coopshield.soc.identity.infrastructure.web;

import com.coopshield.soc.identity.application.AuthenticationResult;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static TokenResponse from(AuthenticationResult result) {
        return new TokenResponse(
                result.accessToken(),
                result.accessTokenExpiresAt(),
                result.refreshToken(),
                result.refreshTokenExpiresAt());
    }
}
