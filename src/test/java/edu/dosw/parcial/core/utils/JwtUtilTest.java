package edu.dosw.parcial.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "dosw-ecixpress-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtil.generateToken("user@eci.edu.co", "ROLE_CLIENT");
        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("user@eci.edu.co", "ROLE_CLIENT");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@eci.edu.co");
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("user@eci.edu.co", "ROLE_CAFETERIA_LADY");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_CAFETERIA_LADY");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user@eci.edu.co", "ROLE_CLIENT");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("user@eci.edu.co", "ROLE_CLIENT") + "tampered";
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_blankToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("user@eci.edu.co", "ROLE_CLIENT");
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void getExpiration_returnsExpirationInSeconds() {
        assertThat(jwtUtil.getExpiration()).isEqualTo(86400L);
    }
}
