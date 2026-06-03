package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {

    Optional<Battle> findByIdAndUserId(Long id, Long userId);

    Optional<Battle> findTopByUserIdAndRewardsClaimedFalseOrderByCreatedAtDesc(Long userId);

    // 미수령 + 7일 지난 배틀 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Battle b WHERE b.rewardsClaimed = false AND b.createdAt < :cutoff")
    int deleteExpiredPendingBattles(LocalDateTime cutoff);

    // 보상 수령 완료 + 30일 지난 배틀 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Battle b WHERE b.rewardsClaimed = true AND b.createdAt < :cutoff")
    int deleteOldCompletedBattles(LocalDateTime cutoff);
}