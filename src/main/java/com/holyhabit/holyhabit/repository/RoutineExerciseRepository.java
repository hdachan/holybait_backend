package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {

    // 루틴의 운동 목록 순서대로 조회
    List<RoutineExercise> findAllByRoutineIdOrderByOrderIndex(Long routineId);

    // 루틴 삭제 시 연결된 운동 전체 삭제
    @Modifying
    @Query("DELETE FROM RoutineExercise re WHERE re.routine.id = :routineId")
    void deleteAllByRoutineId(Long routineId);
}
