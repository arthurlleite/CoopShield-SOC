package com.coopshield.soc.simulation.domain;

public class UnknownCharacterException extends RuntimeException {

    public UnknownCharacterException(String characterId) {
        super("Unknown character: " + characterId);
    }
}
