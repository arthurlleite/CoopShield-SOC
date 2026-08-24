package com.coopshield.soc.simulation.domain;

import java.util.List;
import java.util.Optional;

/**
 * Catalogo fixo dos seis personagens sinteticos do laboratorio, um por
 * perfil (ver docs/product/personas-use-cases.md secao 1). Os nomes e
 * unidades sao ficticios, criados exclusivamente para fins educacionais.
 */
public final class Characters {

    public static final Character ANA_BEATRIZ = new Character(
            "ana-beatriz", "Ana Beatriz (Analista de SOC)",
            "synthetic-soc-analyst-01", "SOC_ANALYST", "synthetic-soc-hq");

    public static final Character CARLOS_EDUARDO = new Character(
            "carlos-eduardo", "Carlos Eduardo (Gerente de SOC)",
            "synthetic-soc-manager-01", "SOC_MANAGER", "synthetic-soc-hq");

    public static final Character FERNANDA_LIMA = new Character(
            "fernanda-lima", "Fernanda Lima (Atendente)",
            "synthetic-employee-01", "EMPLOYEE", "synthetic-branch-001");

    public static final Character ROBERTO_NOGUEIRA = new Character(
            "roberto-nogueira", "Roberto Nogueira (Gerente de Unidade)",
            "synthetic-branch-manager-01", "BRANCH_MANAGER", "synthetic-branch-002");

    public static final Character MARINA_SOUZA = new Character(
            "marina-souza", "Marina Souza (Administradora de TI)",
            "synthetic-it-admin-01", "IT_ADMIN", "synthetic-it-hq");

    public static final Character PATRICIA_GOMES = new Character(
            "patricia-gomes", "Patricia Gomes (Auditora)",
            "synthetic-auditor-01", "AUDITOR", "synthetic-audit-hq");

    private static final List<Character> ALL = List.of(
            ANA_BEATRIZ, CARLOS_EDUARDO, FERNANDA_LIMA, ROBERTO_NOGUEIRA, MARINA_SOUZA, PATRICIA_GOMES);

    private Characters() {
    }

    public static List<Character> all() {
        return ALL;
    }

    public static Optional<Character> findById(String id) {
        return ALL.stream().filter(character -> character.id().equals(id)).findFirst();
    }
}
