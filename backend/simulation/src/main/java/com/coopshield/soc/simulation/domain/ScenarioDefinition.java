package com.coopshield.soc.simulation.domain;

import java.util.List;

/**
 * Um cenario sintetico executavel pelo laboratorio: uma sequencia nomeada e
 * documentada de eventos brutos, gerada para um {@link Character} e uma
 * quantidade de eventos solicitada.
 */
public interface ScenarioDefinition {

    String id();

    String displayName();

    String description();

    int defaultEventCount();

    List<GeneratedEvent> generate(Character actor, int eventCount);
}
