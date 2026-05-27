package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.BattleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BattleLogRepository extends JpaRepository<BattleLog, Long> {
    List<BattleLog> findAllByBattleIdOrderByTurnAsc(Long battleId);
}
