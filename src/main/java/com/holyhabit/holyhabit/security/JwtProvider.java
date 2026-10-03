package com.holyhabit.holyhabit.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // 토큰 종류 — refresh 토큰을 access 자리에 쓰지 못하게 구분
    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim(TYPE_CLAIM, TYPE_ACCESS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim(TYPE_CLAIM, TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    // API 인증에 쓸 수 있는 access 토큰인지 (validate 가 VALID 인 토큰에만 호출)
    public boolean isAccessToken(String token) {
        Claims claims = getClaims(token);
        String type = claims.get(TYPE_CLAIM, String.class);
        if (type != null) return TYPE_ACCESS.equals(type);
        // type 이 없는 건 이 변경 이전에 발급된 토큰 — access 토큰에만 role 이 있음
        // (기존 access 토큰은 발급 후 1시간이면 모두 만료되므로 그 뒤로는 의미 없는 분기)
        return claims.get("role") != null;
    }

    public JwtValidationResult validate(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return JwtValidationResult.VALID;
        } catch (ExpiredJwtException e) {
            return JwtValidationResult.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return JwtValidationResult.INVALID;
        }
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public enum JwtValidationResult {
        VALID, EXPIRED, INVALID
    }
}
