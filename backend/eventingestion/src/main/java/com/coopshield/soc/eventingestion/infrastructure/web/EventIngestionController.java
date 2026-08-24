package com.coopshield.soc.eventingestion.infrastructure.web;

import com.coopshield.soc.eventingestion.application.EventIngestionService;
import com.coopshield.soc.eventingestion.domain.RawEvent;
import com.coopshield.soc.eventingestion.domain.RawEventValidationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Recebe eventos sinteticos brutos e os publica em {@code security.raw-events}
 * apos validacao de borda. Nao persiste nada diretamente: a persistencia e
 * responsabilidade de {@code eventnormalization}, apos consumir o topico
 * (ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md).
 */
@RestController
public class EventIngestionController {

    private final EventIngestionService ingestionService;

    public EventIngestionController(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/api/v1/events")
    public ResponseEntity<RawEventResponse> ingest(@Valid @RequestBody RawEventRequest request) {
        RawEvent event = ingestionService.ingest(
                request.eventId(), request.eventVersion(), request.eventType(), request.timestamp(),
                request.source(), request.actorUserId(), request.actorRole(), request.actorUnit(),
                request.targetResourceType(), request.targetResourceId(), request.action(), request.outcome(),
                request.deviceId(), request.deviceKnown(), request.sourceIp(), request.geo(),
                request.correlationId(), request.metadata());
        return ResponseEntity.accepted().body(RawEventResponse.from(event));
    }

    @ExceptionHandler(RawEventValidationException.class)
    public ResponseEntity<ApiError> handleValidationFailure(RawEventValidationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("invalid_event", "O evento enviado nao passou na validacao.", e.violations()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBeanValidationFailure(MethodArgumentNotValidException e) {
        List<String> violations = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("invalid_event", "O evento enviado nao passou na validacao.", violations));
    }
}
