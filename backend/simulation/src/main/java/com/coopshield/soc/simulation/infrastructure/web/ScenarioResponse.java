package com.coopshield.soc.simulation.infrastructure.web;

import com.coopshield.soc.simulation.domain.Scenario;

public record ScenarioResponse(String id, String name, String description, int defaultEventCount) {

    public static ScenarioResponse from(Scenario scenario) {
        return new ScenarioResponse(scenario.id(), scenario.displayName(), scenario.description(), scenario.defaultEventCount());
    }
}
