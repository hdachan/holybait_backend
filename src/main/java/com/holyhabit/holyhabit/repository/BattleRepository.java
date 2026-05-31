package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {

    Optional<Battle> findByIdAndUserId(Long id, Long userId);

    // 미수령 배틀 조회 (중간에 나갔다 들어왔을 때 체크)
    Optional<Battle> findTopByUserIdAndRewardsClaimedFalseOrderByCreatedAtDesc(Long userId);
}
