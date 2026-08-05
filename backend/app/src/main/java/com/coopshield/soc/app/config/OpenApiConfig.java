package com.coopshield.soc.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados do OpenAPI exposto pela aplicacao. Os endpoints de negocio sao
 * adicionados nas fases seguintes (ver docs/roadmap.md); esta configuracao
 * garante que /v3/api-docs e /swagger-ui.html ja fiquem disponiveis desde a
 * Fase 1, descrevendo o projeto e o aviso de independencia obrigatorio.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coopShieldSocOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CoopShield SOC API")
                        .version("0.1.0")
                        .description("""
                                API do CoopShield SOC: plataforma educacional de defesa \
                                cibernetica e protecao de dados para um ambiente financeiro \
                                ficticio. Projeto independente, educacional e de portfolio. \
                                Nao possui vinculo com instituicoes financeiras ou empresas \
                                reais. Todos os dados, usuarios, eventos, transacoes e \
                                incidentes apresentados sao sinteticos.""")
                        .contact(new Contact()
                                .name("Arthur Carvalho Leite")
                                .url("https://github.com/arthurlleite/CoopShield-SOC"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
