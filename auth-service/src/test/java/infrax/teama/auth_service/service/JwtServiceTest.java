package infrax.teama.auth_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;
    private static final String TEST_SECRET = "dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2VjcmV0a2V5dGVzdA==";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        testUser = new User("testuser", "password", authorities);
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertTrue(token.contains("."));
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGenerateTokenWithDifferentUsers() {
        Collection<SimpleGrantedAuthority> userAuth = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetails regularUser = new User("regularuser", "password", userAuth);

        String adminToken = jwtService.generateToken(testUser);
        String userToken = jwtService.generateToken(regularUser);

        assertNotEquals(adminToken, userToken);
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(testUser);
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testExtractUsernameFromDifferentToken() {
        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetails differentUser = new User("differentuser", "password", authorities);
        String token = jwtService.generateToken(differentUser);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("differentuser", extractedUsername);
    }

    @Test
    void testIsTokenValidWithCorrectUser() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidWithDifferentUser() {
        String token = jwtService.generateToken(testUser);

        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetails differentUser = new User("differentuser", "password", authorities);

        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    void testTokenContainsRoles() {
        String token = jwtService.generateToken(testUser);

        // Decode and verify roles are in the token
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertTrue(payload.contains("ROLE_ADMIN"));
        assertTrue(payload.contains("roles"));
    }

    @Test
    void testTokenContainsSubject() {
        String token = jwtService.generateToken(testUser);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testGenerateTokenWithMultipleRoles() {
        Collection<SimpleGrantedAuthority> authorities = java.util.Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetails multiRoleUser = new User("multiuser", "password", authorities);

        String token = jwtService.generateToken(multiRoleUser);

        assertNotNull(token);
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertTrue(payload.contains("ROLE_ADMIN"));
        assertTrue(payload.contains("ROLE_USER"));
    }

    @Test
    void testGenerateTokenWithNoRoles() {
        Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
        UserDetails userWithoutRoles = new User("noroleuser", "password", authorities);

        String token = jwtService.generateToken(userWithoutRoles);

        assertNotNull(token);
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertTrue(payload.contains("roles"));
        assertTrue(payload.contains("[]"));
    }

    @Test
    void testTokenNotValidWithInvalidSignature() {
        String token = jwtService.generateToken(testUser);

        // Try to validate with a different secret (simulating tampering)
        assertThrows(Exception.class, () -> {
            String tamperedToken = token.substring(0, token.length() - 10) + "tampered!!";
            jwtService.isTokenValid(tamperedToken, testUser);
        });
    }

    @Test
    void testGenerateConsistentTokens() {
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(testUser);

        // Tokens should be different due to timestamp
        assertNotEquals(token1, token2);

        // But should both be valid
        assertTrue(jwtService.isTokenValid(token1, testUser));
        assertTrue(jwtService.isTokenValid(token2, testUser));
    }

    @Test
    void testExtractUsernameMultipleTimes() {
        String token = jwtService.generateToken(testUser);

        String username1 = jwtService.extractUsername(token);
        String username2 = jwtService.extractUsername(token);

        assertEquals(username1, username2);
        assertEquals("testuser", username1);
    }
}

