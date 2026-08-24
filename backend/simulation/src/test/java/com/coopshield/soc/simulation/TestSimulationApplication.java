package com.coopshield.soc.simulation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuracao minima de bootstrap para os testes {@code @SpringBootTest}
 * deste modulo. Ao contrario de eventingestion/eventnormalization, o modulo
 * {@code simulation} depende em tempo de execucao de beans de
 * {@code eventingestion} ({@code EventIngestionService}, o publisher Kafka),
 * entao o component scan precisa cobrir {@code com.coopshield.soc} inteiro,
 * nao apenas o pacote deste modulo - mesmo padrao de
 * {@code com.coopshield.soc.app.CoopShieldSocApplication}. A aplicacao real
 * e essa mesma classe.
 */
@SpringBootApplication(scanBasePackages = "com.coopshield.soc")
@EnableMongoRepositories(basePackages = "com.coopshield.soc")
class TestSimulationApplication {
}
