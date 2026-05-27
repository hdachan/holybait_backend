package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.CharacterStat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CharacterStatRepository extends JpaRepository<CharacterStat, Long> {
    Optional<CharacterStat> findByUserId(Long userId);
}
