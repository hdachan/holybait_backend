package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    // 내 루틴 전체 조회
    List<Routine> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 내 루틴 단건 조회 (본인 것인지 확인)
    Optional<Routine> findByIdAndUserId(Long id, Long userId);
}
