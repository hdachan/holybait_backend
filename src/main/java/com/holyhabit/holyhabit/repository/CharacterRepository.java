package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.GameCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CharacterRepository extends JpaRepository<GameCharacter, Long> {
    List<GameCharacter> findAllByOrderByIdAsc();
}
