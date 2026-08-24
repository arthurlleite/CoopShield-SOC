package com.coopshield.soc.eventnormalization;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Configuracao minima de bootstrap para os testes {@code @SpringBootTest}
 * deste modulo. O modulo {@code eventnormalization} e uma biblioteca (jar),
 * nao uma aplicacao Spring Boot; esta classe existe apenas para que o
 * mecanismo de busca do Spring Boot Test encontre uma
 * {@code @SpringBootConfiguration} ao rodar os testes isoladamente, com
 * component scan cobrindo os beans do proprio modulo (listener Kafka,
 * repositorios Mongo). A aplicacao real e
 * {@code com.coopshield.soc.app.CoopShieldSocApplication}.
 */
@SpringBootApplication
class TestEventNormalizationApplication {
}
