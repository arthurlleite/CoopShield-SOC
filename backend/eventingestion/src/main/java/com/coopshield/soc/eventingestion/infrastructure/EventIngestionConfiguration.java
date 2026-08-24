package com.coopshield.soc.eventingestion.infrastructure;

import com.coopshield.soc.eventingestion.application.EventIngestionService;
import com.coopshield.soc.eventingestion.application.RawEventPublisher;
import com.coopshield.soc.eventingestion.infrastructure.kafka.EventTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Composicao dos beans do modulo eventingestion: liga a porta de aplicacao
 * ao adaptador Kafka e declara o topico que este modulo produz (ver ADR-002).
 */
@Configuration
public class EventIngestionConfiguration {

    @Bean
    public EventIngestionService eventIngestionService(RawEventPublisher publisher) {
        return new EventIngestionService(publisher);
    }

    @Bean
    public NewTopic rawEventsTopic() {
        return TopicBuilder.name(EventTopics.RAW_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
