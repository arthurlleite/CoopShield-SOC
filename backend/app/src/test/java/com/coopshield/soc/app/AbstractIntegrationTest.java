package com.coopshield.soc.app;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Classe base para os testes de integracao do modulo {@code app}. Desde a
 * Fase 3, o backend depende de um MongoDB real (o seeder de usuarios
 * sinteticos grava no banco na inicializacao) - ver
 * docs/adr/ADR-012-mongodb-real-fase-3.md.
 *
 * <p>O container e estatico, iniciado uma unica vez (bloco estatico) e
 * deliberadamente nunca parado - ver o padrao "Singleton Container" da
 * documentacao do Testcontainers. Usar {@code @Testcontainers}/{@code @Container}
 * aqui pararia o container ao final da PRIMEIRA classe de teste que o usa
 * (o ciclo de vida da extensao e por classe, mesmo com um campo estatico
 * herdado de uma superclasse), quebrando todas as subclasses seguintes na
 * mesma JVM com "connection refused". O Ryuk do Testcontainers encerra o
 * container ao fim da JVM de teste.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
abstract class AbstractIntegrationTest {

    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    static {
        MONGO_DB_CONTAINER.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }
}
