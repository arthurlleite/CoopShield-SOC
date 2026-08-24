package com.coopshield.soc.simulation.domain;

public class UnknownScenarioException extends RuntimeException {

    public UnknownScenarioException(String scenarioId) {
        super("Unknown scenario: " + scenarioId);
    }
}
