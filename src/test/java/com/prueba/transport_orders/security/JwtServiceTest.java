package com.prueba.transport_orders.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

     private static final String SECRET = "test-secret-key-with-at-least-32-characters";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000);

    @Test
    @DisplayName("Genera un token válido con el usuario como subject")
    void generateToken_returnsValidTokenWithUsername() {
        String token = jwtService.generateToken("admin");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Un token alterado no es válido")
    void isValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken("admin");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Un texto que no es JWT no es válido")
    void isValid_garbage_returnsFalse() {
        assertThat(jwtService.isValid("esto-no-es-un-token")).isFalse();
    }

    @Test
    @DisplayName("Un token expirado no es válido")
    void isValid_expiredToken_returnsFalse() {
        JwtService expiredJwtService = new JwtService(SECRET, -1_000);
        String token = expiredJwtService.generateToken("admin");

        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("Un token firmado con otro secreto no es válido")
    void isValid_differentSecret_returnsFalse() {
        JwtService otherService = new JwtService("another-secret-key-with-at-least-32-chars", 3_600_000);
        String token = otherService.generateToken("admin");

        assertThat(jwtService.isValid(token)).isFalse();
    }

}
