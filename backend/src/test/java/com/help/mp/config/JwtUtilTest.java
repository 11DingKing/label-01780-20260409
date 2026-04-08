package com.help.mp.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-at-least-32-characters-long");
        props.setExpireSeconds(3600L);
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void createToken_and_getUserId() {
        String token = jwtUtil.createToken(100L, "openid_abc");
        assertNotNull(token);
        Long userId = jwtUtil.getUserId(token);
        assertEquals(100L, userId);
    }

    @Test
    void getUserId_invalidToken_returnsNull() {
        assertNull(jwtUtil.getUserId(null));
        assertNull(jwtUtil.getUserId(""));
        assertNull(jwtUtil.getUserId("invalid.token.here"));
    }

    @Test
    void verify_validToken_returnsDecodedJWT() {
        String token = jwtUtil.createToken(1L, "oid");
        assertNotNull(jwtUtil.verify(token));
        assertEquals(1L, Long.parseLong(jwtUtil.verify(token).getSubject()));
    }

    @Test
    void verify_invalidToken_returnsNull() {
        assertNull(jwtUtil.verify("invalid"));
    }
}
