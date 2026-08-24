package com.coopshield.soc.detection;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Configuracao minima de bootstrap para os testes {@code @SpringBootTest}
 * deste modulo - ver com.coopshield.soc.eventnormalization.TestEventNormalizationApplication
 * para a mesma justificativa. Ao contrario de {@code simulation}, este
 * modulo nao depende de nenhum outro modulo alem de {@code sharedkernel},
 * entao o component scan padrao (apenas este pacote) e suficiente.
 */
@SpringBootApplication
class TestDetectionApplication {
}
