package com.coopshield.soc.eventnormalization.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IpHasherTest {

    @Test
    void producesTheSameHashForTheSameInput() {
        assertThat(IpHasher.hash("203.0.113.10")).isEqualTo(IpHasher.hash("203.0.113.10"));
    }

    @Test
    void producesDifferentHashesForDifferentInputs() {
        assertThat(IpHasher.hash("203.0.113.10")).isNotEqualTo(IpHasher.hash("203.0.113.11"));
    }

    @Test
    void neverReturnsThePlaintextIp() {
        assertThat(IpHasher.hash("203.0.113.10")).doesNotContain("203.0.113.10");
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> IpHasher.hash(null)).isInstanceOf(NullPointerException.class);
    }
}
