package com.coopshield.soc.simulation.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record StartRunRequest(@NotBlank String scenarioId, @NotBlank String characterId, Integer eventCount) {
}
