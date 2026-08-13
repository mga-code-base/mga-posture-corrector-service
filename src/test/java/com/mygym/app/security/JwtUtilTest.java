package com.mygym.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "superSecretKeyForJwtSigningThatIsAtLeast256BitsLongForHMACSHA256!";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "encryptionSecret", testSecret);
    }

    @Test
    @DisplayName("Should successfully generate token and extract username and role claims")
    void testGenerateTokenAndExtract_Success() {
        String username = "posture_user";
        String role = "MEMBER";

        String token = jwtUtil.generateToken(username, role);

        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
        
        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals(role, claims.get("role", String.class));
    }

    @Test
    @DisplayName("Should validate token successfully for matching username")
    void testValidateToken_ValidToken_ReturnsTrue() {
        String username = "member_user";
        String token = jwtUtil.generateToken(username, "MEMBER");

        boolean isValid = jwtUtil.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should fail token validation when username mismatches")
    void testValidateToken_MismatchedUsername_ReturnsFalse() {
        String token = jwtUtil.generateToken("user_a", "MEMBER");

        boolean isValid = jwtUtil.validateToken(token, "user_b");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw SignatureException when attempting to parse tampered token")
    void testExtractClaims_TamperedToken_ThrowsException() {
        String token = jwtUtil.generateToken("user_a", "MEMBER");
        String tamperedToken = token + "invalid_signature";

        assertThrows(SignatureException.class, () -> jwtUtil.extractAllClaims(tamperedToken));
    }
}