package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.entity.RefreshToken;
import com.holyhabit.holyhabit.entity.User;
import com.holyhabit.holyhabit.repository.RefreshTokenRepository;
import com.holyhabit.holyhabit.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // Refresh Token 저장 (SHA-256 해시)
    @Transactional
    public void saveRefreshToken(User user, String rawToken, String deviceInfo, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtProvider.getRefreshTokenExpiration() / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    // Access Token 재발급 + Rotation
    @Transactional
    public TokenPair refresh(String rawRefreshToken, String deviceInfo, String ipAddress) {
        String tokenHash = hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("401_003"));

        // Reuse Detection — 이미 폐기된 토큰이 들어오면 전체 세션 폐기
        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            log.warn("Reuse detected. All tokens revoked. userId={}", stored.getUser().getId());
            throw new RuntimeException("401_004");
        }

        // 만료 확인
        if (stored.isExpired()) {
            throw new RuntimeException("401_003");
        }

        // 기존 토큰 폐기 (Rotation)
        stored.revoke();

        User user = stored.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), "USER");
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());

        saveRefreshToken(user, newRefreshToken, deviceInfo, ipAddress);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    // 단일 기기 로그아웃
    @Transactional
    public void revokeToken(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(RefreshToken::revoke);
    }

    // 전체 기기 로그아웃
    @Transactional
    public void revokeAllTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // 만료 토큰 정리 (매일 새벽 3시)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
        log.info("Expired refresh tokens cleaned");
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Hash failed", e);
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
