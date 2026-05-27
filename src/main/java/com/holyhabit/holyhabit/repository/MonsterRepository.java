package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MonsterRepository extends JpaRepository<Monster, Long> {

    List<Monster> findAllByStageId(Long stageId);

    // 스테이지에서 랜덤 몬스터 1마리
    @Query(value = "SELECT * FROM monsters WHERE stage_id = :stageId ORDER BY RAND() LIMIT 1",
           nativeQuery = true)
    Optional<Monster> findRandomByStageId(Long stageId);
}
