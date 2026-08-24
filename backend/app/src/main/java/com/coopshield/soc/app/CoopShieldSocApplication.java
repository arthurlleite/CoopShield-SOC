package com.coopshield.soc.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Ponto de entrada do monolito modular CoopShield SOC.
 *
 * <p>Projeto independente, educacional e de portfolio. Nao possui vinculo
 * com instituicoes financeiras ou empresas reais. Todos os dados, usuarios,
 * eventos, transacoes e incidentes processados sao sinteticos.
 */
@SpringBootApplication(scanBasePackages = "com.coopshield.soc")
@EnableMongoRepositories(basePackages = "com.coopshield.soc")
public class CoopShieldSocApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoopShieldSocApplication.class, args);
    }
}
