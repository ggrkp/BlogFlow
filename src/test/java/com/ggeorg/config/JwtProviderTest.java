package com.ggeorg.config;

import com.ggeorg.domain.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    // HS256 needs a key of at least 256 bits; this test secret is 48 bytes and is not used anywhere else.
    private static final String TEST_SECRET = "unit-test-only-secret-key-with-more-than-256-bits";

    private JwtProvider jwtProvider;
    private Author author;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", 60_000L);
        author = Author.builder().id(42L).username("test_user").email("test@example.com").build();
    }

    @Test
    void generatedTokenIsValidAndCarriesUserId() {
        String token = jwtProvider.generateToken(author);

        assertTrue(jwtProvider.validateToken(token));
        assertEquals(42L, jwtProvider.getUserIdFromToken(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtProvider.generateToken(author);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("aa") ? "bb" : "aa");

        assertFalse(jwtProvider.validateToken(tampered));
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        String token = jwtProvider.generateToken(author);

        JwtProvider other = new JwtProvider();
        ReflectionTestUtils.setField(other, "jwtSecret", "a-completely-different-secret-key-of-sufficient-length");
        ReflectionTestUtils.setField(other, "jwtExpirationMs", 60_000L);

        assertFalse(other.validateToken(token));
    }

    @Test
    void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", -1_000L);
        String token = jwtProvider.generateToken(author);

        assertFalse(jwtProvider.validateToken(token));
    }

    @Test
    void garbageInputIsRejected() {
        assertFalse(jwtProvider.validateToken("not-a-jwt"));
    }
}
