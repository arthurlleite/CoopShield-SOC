package com.coopshield.soc.simulation.domain;

public class InvalidEventCountException extends RuntimeException {

    public InvalidEventCountException(int requested, int min, int max) {
        super("eventCount must be between " + min + " and " + max + " but was " + requested);
    }
}
