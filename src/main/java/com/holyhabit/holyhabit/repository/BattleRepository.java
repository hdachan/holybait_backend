package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {
    Optional<Battle> findByIdAndUserId(Long id, Long userId);
}
