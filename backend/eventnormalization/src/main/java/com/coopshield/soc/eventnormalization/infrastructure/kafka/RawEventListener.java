package com.coopshield.soc.eventnormalization.infrastructure.kafka;

import com.coopshield.soc.eventnormalization.application.EventNormalizationService;
import com.coopshield.soc.eventnormalization.domain.EventNormalizationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consome {@code security.raw-events}, normaliza e delega a
 * {@link EventNormalizationService}. Mensagens que falham a normalizacao
 * lancam {@link EventNormalizationException}, que o error handler do
 * container (configurado em
 * {@code com.coopshield.soc.eventnormalization.infrastructure.EventNormalizationConfiguration})
 * encaminha para {@code security.dead-letter} apos as tentativas de retry -
 * ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 *
 * <p>{@code auto.offset.reset=earliest} e fixado aqui, no listener, e nao
 * apenas em {@code application.yml} do modulo {@code app}: a garantia de
 * idempotencia via indice unico no MongoDB (ver
 * {@code MongoNormalizedEventRepository}) so vale se este consumidor nunca
 * pular mensagens publicadas antes dele se inscrever - o que "latest" (o
 * padrao do cliente Kafka) permitiria. Deixar essa garantia depender de uma
 * configuracao externa ao modulo teria dois problemas: silenciosamente
 * voltar a "latest" em qualquer aplicacao que monte este modulo sem copiar a
 * config, e - como observado nos testes de integracao deste modulo quando
 * executados isoladamente (sem application.yml do app) - permitir uma
 * corrida real entre a publicacao do evento de teste e o primeiro reset de
 * offset do consumidor.
 */
@Component
public class RawEventListener {

    private final EventNormalizationService normalizationService;
    private final ObjectMapper objectMapper;

    public RawEventListener(EventNormalizationService normalizationService, ObjectMapper objectMapper) {
        this.normalizationService = normalizationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = EventTopics.RAW_EVENTS,
            groupId = "eventnormalization",
            properties = "auto.offset.reset=earliest")
    public void onRawEvent(String payload) {
        RawEventMessage message = parse(payload);

        RawEventMessage.Actor actor = message.actor();
        RawEventMessage.Target target = message.target();
        RawEventMessage.Device device = message.device();

        normalizationService.normalize(
                message.eventId(),
                message.eventVersion(),
                message.eventType(),
                message.timestamp(),
                message.source(),
                actor == null ? null : actor.userId(),
                actor == null ? null : actor.role(),
                actor == null ? null : actor.unit(),
                target == null ? null : target.resourceType(),
                target == null ? null : target.resourceId(),
                message.action(),
                message.outcome(),
                device == null ? null : device.deviceId(),
                device != null && device.known(),
                message.sourceIp(),
                message.geo(),
                message.correlationId(),
                message.metadata());
    }

    private RawEventMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, RawEventMessage.class);
        } catch (Exception e) {
            throw new EventNormalizationException(List.of("payload is not a valid raw event JSON message"));
        }
    }
}
