package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    // 특정 운동의 가장 최근 기록 조회 (운동기록화면 최근 기록 표시용)
    Optional<WorkoutLog> findTopByUserIdAndRoutineExerciseIdOrderByLoggedAtDesc(
            Long userId, Long routineExerciseId);

    // 내 전체 운동 기록 조회
    List<WorkoutLog> findAllByUserIdOrderByLoggedAtDesc(Long userId);

    List<WorkoutLog> findAllByRoutineExerciseId(Long routineExerciseId);

    @Modifying
    @Query("DELETE FROM WorkoutLog w WHERE w.routineExercise.id = :routineExerciseId")
    void deleteAllByRoutineExerciseId(Long routineExerciseId);
}
