package com.coopshield.soc.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class CoopShieldSocApplicationTests {

    @Test
    void applicationContextLoads() {
        // Verifica que o contexto do Spring sobe com sucesso, com todos os
        // modulos de dominio no classpath, conforme criterio de conclusao
        // da Fase 1 (docs/phase-1-plan.md).
    }
}
