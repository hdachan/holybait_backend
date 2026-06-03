package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.BattleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface BattleLogRepository extends JpaRepository<BattleLog, Long> {

    List<BattleLog> findAllByBattleIdOrderByTurnAsc(Long battleId);

    // 보상 수령 시 즉시 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM BattleLog bl WHERE bl.battle.id = :battleId")
    void deleteAllByBattleId(Long battleId);

    // 스케줄러용 — 미수령 7일 지난 배틀의 로그 삭제
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM BattleLog bl
        WHERE bl.battle.id IN (
            SELECT b.id FROM Battle b
            WHERE b.rewardsClaimed = false
            AND b.createdAt < :cutoff
        )
        """)
    int deleteLogsOfExpiredPendingBattles(LocalDateTime cutoff);
}