package com.coopshield.soc.eventingestion.infrastructure.web;

import com.coopshield.soc.eventingestion.domain.RawEvent;

public record RawEventResponse(String eventId, String correlationId) {

    public static RawEventResponse from(RawEvent event) {
        return new RawEventResponse(event.eventId().toString(), event.correlationId().toString());
    }
}
