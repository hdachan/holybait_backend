package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // 특정 유저의 전체 토큰 폐기 (Reuse Detection / 전체 로그아웃)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE r.user.id = :userId AND r.revoked = false")
    void revokeAllByUserId(Long userId);

    // 만료된 토큰 삭제 (스케줄러용)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
