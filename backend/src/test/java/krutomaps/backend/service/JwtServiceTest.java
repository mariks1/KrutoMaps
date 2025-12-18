package krutomaps.backend.service;

import krutomaps.backend.entity.RoleEntity;
import krutomaps.backend.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("this-is-a-secure-key-32bytes-long!!".getBytes(StandardCharsets.UTF_8));
    private static final String OTHER_SECRET = Base64.getEncoder()
            .encodeToString("another-secure-key-32bytes-long!!!".getBytes(StandardCharsets.UTF_8));

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void generatesAndValidatesToken() {
        UserEntity user = testUser("alice", "USER");

        String token = jwtService.generateToken(user);

        assertEquals("alice", jwtService.extractUserName(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void tokenSignedWithAnotherSecretIsInvalid() {
        UserEntity user = testUser("bob", "ADMIN");

        String token = jwtService.generateToken(user);

        JwtService otherService = new JwtService(OTHER_SECRET);
        assertFalse(otherService.isTokenValid(token, user));
    }

    @Test
    void rejectsTooShortSecret() {
        String shortSecret = Base64.getEncoder()
                .encodeToString("tiny-secret".getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new JwtService(shortSecret));
        assertTrue(ex.getMessage().contains("at least 256 bits"));
    }

    private static UserEntity testUser(String username, String roleName) {
        RoleEntity role = RoleEntity.builder()
                .roleName(roleName)
                .build();

        return UserEntity.builder()
                .id(1L)
                .username(username)
                .password("dummy")
                .role(role)
                .build();
    }
}
