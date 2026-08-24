package com.coopshield.soc.simulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Os doze cenarios sinteticos do laboratorio (ver especificacao do produto).
 * Cada cenario gera uma sequencia de {@link GeneratedEvent} cujos
 * {@code eventType} correspondem ao catalogo em docs/event-catalog/events.md,
 * pensada para futuramente acionar as regras de deteccao correspondentes
 * (Fase 6).
 */
public enum Scenario implements ScenarioDefinition {

    NORMAL("normal", "Atividade normal", "Login bem-sucedido seguido de consultas e logout, em dispositivo conhecido.", 3) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            events.add(authEvent(device, true, "authentication.login.success", "SUCCESS", Map.of()));
            for (int i = 1; i < eventCount - 1; i++) {
                events.add(dataEvent(device, "data.access.query", "QUERY", "SUCCESS", Map.of()));
            }
            if (eventCount > 1) {
                events.add(authEvent(device, true, "authentication.logout", "SUCCESS", Map.of()));
            }
            return capTo(events, eventCount);
        }
    },

    ACCOUNT_COMPROMISED("account-compromised", "Conta possivelmente comprometida",
            "Varias falhas de login seguidas de sucesso, em dispositivo desconhecido - jornada de referencia da Fase 0.", 6) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("unknown");
            int failures = Math.max(1, eventCount - 2);
            for (int i = 0; i < failures; i++) {
                events.add(authEvent(device, false, "authentication.login.failure", "FAILURE", Map.of()));
            }
            events.add(new GeneratedEvent("device.unrecognized", "LOGIN", "FAILURE", "account",
                    SyntheticData.randomResourceId("account"), device, false, SyntheticData.randomSyntheticIp(), "synthetic-region", Map.of()));
            events.add(authEvent(device, false, "authentication.login.success", "SUCCESS", Map.of()));
            return capTo(events, eventCount);
        }
    },

    PRIVILEGE_ABUSE("privilege-abuse", "Abuso de privilegio",
            "Acoes de exportacao/consulta fora do padrao esperado para o perfil do ator.", 3) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(dataEvent(device, "data.access.export", "EXPORT", "SUCCESS",
                        Map.of("outOfPattern", "true")));
            }
            return events;
        }
    },

    MASS_QUERY("mass-query", "Consulta massiva",
            "Grande volume de consultas a registros ficticios em curto intervalo.", 20) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(dataEvent(device, "data.access.query", "QUERY", "SUCCESS", Map.of()));
            }
            return events;
        }
    },

    ATYPICAL_EXPORT("atypical-export", "Exportacao atipica",
            "Exportacao de dados ficticios em volume ou padrao fora do esperado.", 3) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(dataEvent(device, "data.access.export", "EXPORT", "SUCCESS",
                        Map.of("recordCount", "5000")));
            }
            return events;
        }
    },

    PII_EXPOSED("pii-exposed", "Dado sensivel exposto",
            "Deteccao de dado classificado como sensivel em texto puro (violacao de politica).", 2) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(dataEvent(device, "data.access.sensitive.exposure", "QUERY", "FAILURE",
                        Map.of("fieldType", "synthetic-customer-id")));
            }
            return events;
        }
    },

    ADMIN_CHANGE("admin-change", "Alteracao administrativa",
            "Criacao de conta ou alteracao de permissao de usuario ficticio.", 2) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            String[] types = {"admin.permission.changed", "admin.account.created", "admin.account.disabled"};
            for (int i = 0; i < eventCount; i++) {
                events.add(new GeneratedEvent(types[i % types.length], "ADMIN_UPDATE", "SUCCESS", "permission",
                        SyntheticData.randomResourceId("permission"), device, true, SyntheticData.randomSyntheticIp(),
                        "synthetic-region", Map.of()));
            }
            return events;
        }
    },

    API_ANOMALY("api-anomaly", "Anomalia de API",
            "Pico de respostas de erro (401/403/5xx) em endpoint monitorado.", 8) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(new GeneratedEvent("api.response.error", "REQUEST", "FAILURE", "api-endpoint",
                        SyntheticData.randomResourceId("endpoint"), device, true, SyntheticData.randomSyntheticIp(),
                        "synthetic-region", Map.of("httpStatus", i % 2 == 0 ? "401" : "500")));
            }
            return events;
        }
    },

    ATYPICAL_AUTH("atypical-auth", "Autenticacao atipica",
            "Login em horario ou regiao fora do padrao habitual do ator.", 2) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(new GeneratedEvent("authentication.login.success", "LOGIN", "SUCCESS", "account",
                        SyntheticData.randomResourceId("account"), device, true, SyntheticData.randomSyntheticIp(),
                        "synthetic-unusual-region", Map.of("hourOfDay", "03")));
            }
            return events;
        }
    },

    UNKNOWN_DEVICE("unknown-device", "Dispositivo desconhecido",
            "Login bem-sucedido a partir de um dispositivo nao reconhecido na linha de base.", 2) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("unknown");
            events.add(new GeneratedEvent("device.unrecognized", "LOGIN", "FAILURE", "account",
                    SyntheticData.randomResourceId("account"), device, false, SyntheticData.randomSyntheticIp(), "synthetic-region", Map.of()));
            for (int i = 1; i < eventCount; i++) {
                events.add(authEvent(device, false, "authentication.login.success", "SUCCESS", Map.of()));
            }
            return capTo(events, eventCount);
        }
    },

    FAILURES_THEN_SUCCESS("failures-then-success", "Falhas seguidas de sucesso",
            "Tentativas de login falhas seguidas de sucesso, em dispositivo conhecido (sem troca de dispositivo).", 4) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            int failures = Math.max(1, eventCount - 1);
            for (int i = 0; i < failures; i++) {
                events.add(authEvent(device, true, "authentication.login.failure", "FAILURE", Map.of()));
            }
            events.add(authEvent(device, true, "authentication.login.success", "SUCCESS", Map.of()));
            return capTo(events, eventCount);
        }
    },

    UNAUTHORIZED_ENDPOINT("unauthorized-endpoint", "Endpoint nao autorizado",
            "Tentativas de acesso negadas a um recurso fora do perfil do ator.", 2) {
        @Override
        public List<GeneratedEvent> generate(Character actor, int eventCount) {
            List<GeneratedEvent> events = new ArrayList<>();
            String device = SyntheticData.randomDeviceId("known");
            for (int i = 0; i < eventCount; i++) {
                events.add(new GeneratedEvent("authorization.access.denied", "ACCESS", "FAILURE", "api-endpoint",
                        SyntheticData.randomResourceId("endpoint"), device, true, SyntheticData.randomSyntheticIp(),
                        "synthetic-region", Map.of()));
            }
            return events;
        }
    };

    private final String id;
    private final String scenarioName;
    private final String description;
    private final int defaultEventCount;

    Scenario(String id, String scenarioName, String description, int defaultEventCount) {
        this.id = id;
        this.scenarioName = scenarioName;
        this.description = description;
        this.defaultEventCount = defaultEventCount;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return scenarioName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public int defaultEventCount() {
        return defaultEventCount;
    }

    static GeneratedEvent authEvent(String device, boolean deviceKnown, String eventType, String outcome, Map<String, String> metadata) {
        return new GeneratedEvent(eventType, "LOGIN", outcome, "account",
                SyntheticData.randomResourceId("account"), device, deviceKnown, SyntheticData.randomSyntheticIp(),
                "synthetic-region", metadata);
    }

    static GeneratedEvent dataEvent(String device, String eventType, String action, String outcome, Map<String, String> metadata) {
        return new GeneratedEvent(eventType, action, outcome, "customer-record",
                SyntheticData.randomResourceId("record"), device, true, SyntheticData.randomSyntheticIp(),
                "synthetic-region", metadata);
    }

    static List<GeneratedEvent> capTo(List<GeneratedEvent> events, int eventCount) {
        if (events.size() <= eventCount) {
            return events;
        }
        return new ArrayList<>(events.subList(0, eventCount));
    }

    public static Optional<Scenario> findById(String id) {
        for (Scenario scenario : values()) {
            if (scenario.id.equals(id)) {
                return Optional.of(scenario);
            }
        }
        return Optional.empty();
    }
}
