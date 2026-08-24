package com.coopshield.soc.simulation.domain;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Geradores de valores sinteticos usados pelos cenarios. Enderecos IP usam
 * exclusivamente os blocos reservados pela IANA para documentacao
 * (TEST-NET-1/2/3, RFC 5737) - nunca roteaveis na internet real, reforcando
 * que nenhum dado aqui e real.
 */
final class SyntheticData {

    private static final List<String> DOCUMENTATION_IP_PREFIXES = List.of("203.0.113.", "198.51.100.", "192.0.2.");
    private static final Random RANDOM = new Random();

    private SyntheticData() {
    }

    static String randomSyntheticIp() {
        String prefix = DOCUMENTATION_IP_PREFIXES.get(RANDOM.nextInt(DOCUMENTATION_IP_PREFIXES.size()));
        return prefix + (1 + RANDOM.nextInt(254));
    }

    static String randomDeviceId(String prefix) {
        return "synthetic-device-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    static String randomResourceId(String prefix) {
        return "synthetic-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
