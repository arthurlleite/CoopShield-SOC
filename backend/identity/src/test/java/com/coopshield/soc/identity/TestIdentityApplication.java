package com.coopshield.soc.identity;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuracao minima de bootstrap para os testes de fatia Spring Boot
 * (ex.: {@code @DataMongoTest}) deste modulo. O modulo {@code identity} e
 * uma biblioteca (jar), nao uma aplicacao Spring Boot; essa classe existe
 * apenas para que o mecanismo de busca do Spring Boot Test encontre uma
 * {@code @SpringBootConfiguration} ao rodar os testes isoladamente. A
 * aplicacao real e {@code com.coopshield.soc.app.CoopShieldSocApplication}.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
class TestIdentityApplication {
}
