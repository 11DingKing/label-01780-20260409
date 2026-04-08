package com.help.mp.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public String createToken(Long userId, String openid) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("openid", openid)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getExpireSeconds() * 1000))
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    public DecodedJWT verify(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public Long getUserId(String token) {
        DecodedJWT jwt = verify(token);
        return jwt != null ? Long.parseLong(jwt.getSubject()) : null;
    }
}
