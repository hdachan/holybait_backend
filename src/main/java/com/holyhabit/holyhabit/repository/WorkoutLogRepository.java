package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    // 가장 최근 log 조회
    Optional<WorkoutLog> findTopByUserIdAndRoutineExerciseIdOrderByLoggedAtDesc(
            Long userId, Long routineExerciseId);

    // 오늘 log 조회 (upsert 용)
    @Query("""
        SELECT w FROM WorkoutLog w
        WHERE w.user.id = :userId
          AND w.routineExercise.id = :routineExerciseId
          AND w.loggedAt >= :from
          AND w.loggedAt < :to
        ORDER BY w.loggedAt DESC
        LIMIT 1
        """)
    Optional<WorkoutLog> findTodayLog(
            Long userId, Long routineExerciseId,
            LocalDateTime from, LocalDateTime to);

    List<WorkoutLog> findAllByRoutineExerciseId(Long routineExerciseId);

    @Modifying
    @Query("DELETE FROM WorkoutLog w WHERE w.routineExercise.id = :routineExerciseId")
    void deleteAllByRoutineExerciseId(Long routineExerciseId);

    // 유저의 전체 운동 로그 (최신순) — 전체 기록 목록용
    List<WorkoutLog> findAllByUserIdOrderByLoggedAtDesc(Long userId);

    // 특정 종목의 운동 로그 (최신순) — 종목 상세 기록용
    @Query("""
        SELECT wl FROM WorkoutLog wl
        WHERE wl.user.id = :userId
          AND wl.routineExercise.exercise.id = :exerciseId
        ORDER BY wl.loggedAt DESC
        """)
    List<WorkoutLog> findAllByUserIdAndExerciseIdOrderByLoggedAtDesc(
            Long userId, Long exerciseId);
}