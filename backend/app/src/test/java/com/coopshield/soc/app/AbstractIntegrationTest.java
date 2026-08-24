package com.coopshield.soc.app;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Classe base para os testes de integracao do modulo {@code app}. Desde a
 * Fase 3, o backend depende de um MongoDB real (o seeder de usuarios
 * sinteticos grava no banco na inicializacao) - ver
 * docs/adr/ADR-012-mongodb-real-fase-3.md. Desde a Fase 4, tambem depende de
 * um Kafka real (ingestao/normalizacao de eventos) - ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 *
 * <p>Os containers sao estaticos, iniciados uma unica vez (bloco estatico) e
 * deliberadamente nunca parados - ver o padrao "Singleton Container" da
 * documentacao do Testcontainers. Usar {@code @Testcontainers}/{@code @Container}
 * aqui pararia os containers ao final da PRIMEIRA classe de teste que os usa
 * (o ciclo de vida da extensao e por classe, mesmo com um campo estatico
 * herdado de uma superclasse), quebrando todas as subclasses seguintes na
 * mesma JVM com "connection refused". O Ryuk do Testcontainers encerra os
 * containers ao fim da JVM de teste.
 *
 * <p>O Kafka de teste usa a imagem Confluent (nao {@code apache/kafka}, usada
 * no docker-compose de execucao): a classe nativa
 * {@code org.testcontainers.kafka.KafkaContainer} com imagens apache/kafka
 * falha em detectar o advertised.listener correto neste ambiente Docker
 * Desktop/Windows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
abstract class AbstractIntegrationTest {

    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    static {
        MONGO_DB_CONTAINER.start();
        KAFKA_CONTAINER.start();
    }

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }
}
