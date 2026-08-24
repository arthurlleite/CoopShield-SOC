package com.coopshield.soc.audit;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuracao minima de bootstrap para os testes de fatia Spring Boot
 * ({@code @DataMongoTest}) deste modulo - ver
 * com.coopshield.soc.identity.TestIdentityApplication para a mesma
 * justificativa.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
class TestAuditApplication {
}
