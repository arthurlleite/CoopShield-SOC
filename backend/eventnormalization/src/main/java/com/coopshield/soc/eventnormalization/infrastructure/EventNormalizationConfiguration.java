package com.coopshield.soc.eventnormalization.infrastructure;

import com.coopshield.soc.eventnormalization.application.EventNormalizationService;
import com.coopshield.soc.eventnormalization.application.NormalizedEventPublisher;
import com.coopshield.soc.eventnormalization.application.NormalizedEventRepository;
import com.coopshield.soc.eventnormalization.domain.EventNormalizationException;
import com.coopshield.soc.eventnormalization.infrastructure.kafka.EventTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Composicao dos beans do modulo eventnormalization: liga as portas de
 * aplicacao aos adaptadores Kafka/MongoDB, declara os topicos que este
 * modulo produz e configura o error handler de retry/dead-letter do
 * consumidor - ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
@Configuration
public class EventNormalizationConfiguration {

    @Bean
    public EventNormalizationService eventNormalizationService(
            NormalizedEventRepository repository, NormalizedEventPublisher publisher) {
        return new EventNormalizationService(repository, publisher);
    }

    /**
     * Modulo sem {@code spring-web}, entao a autoconfiguracao Jackson do
     * Spring Boot (que depende de {@code Jackson2ObjectMapperBuilder}, do
     * modulo spring-web) nao ativa quando este modulo roda sozinho (ex.:
     * seus proprios testes). Dentro do modulo {@code app} completo (que tem
     * spring-web via outros modulos), o bean autoconfigurado do Spring Boot
     * prevalece e este backs off.
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public NewTopic normalizedEventsTopic() {
        return TopicBuilder.name(EventTopics.NORMALIZED_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(EventTopics.DEAD_LETTER).partitions(1).replicas(1).build();
    }

    /**
     * Falhas de normalizacao (campo obrigatorio ausente, JSON invalido) sao
     * permanentes - reprocessar a mesma mensagem produz o mesmo erro, entao
     * {@link EventNormalizationException} vai direto para
     * {@code security.dead-letter} sem retry. Qualquer outra excecao (ex.:
     * MongoDB temporariamente indisponivel) e tratada como transiente: duas
     * tentativas com 500ms de intervalo antes de tambem cair no dead-letter.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                (record, exception) -> new TopicPartition(EventTopics.DEAD_LETTER, -1));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(500L, 2));
        errorHandler.addNotRetryableExceptions(EventNormalizationException.class);
        return errorHandler;
    }
}
