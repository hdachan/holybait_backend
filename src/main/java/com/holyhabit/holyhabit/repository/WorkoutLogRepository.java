package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    // 운동 기준 가장 최근 log 조회 (어느 루틴에서 했든)
    Optional<WorkoutLog> findTopByUserIdAndExerciseIdOrderByLoggedAtDesc(
            Long userId, Long exerciseId);

    // 운동 기준 특정 시각(오늘 0시) 이전의 가장 최근 log — "지난번 기록"
    Optional<WorkoutLog> findTopByUserIdAndExerciseIdAndLoggedAtBeforeOrderByLoggedAtDesc(
            Long userId, Long exerciseId, LocalDateTime before);

    // 운동 기준 오늘 log 조회 (upsert 용 — 같은 날 같은 운동은 기록 1개)
    @Query("""
        SELECT w FROM WorkoutLog w
        WHERE w.user.id = :userId
          AND w.exercise.id = :exerciseId
          AND w.loggedAt >= :from
          AND w.loggedAt < :to
        ORDER BY w.loggedAt DESC
        LIMIT 1
        """)
    Optional<WorkoutLog> findTodayLog(
            Long userId, Long exerciseId,
            LocalDateTime from, LocalDateTime to);

    // 루틴에서 운동이 빠질 때 — 기록은 남기고 루틴 연결만 끊음
    @Modifying
    @Query("UPDATE WorkoutLog w SET w.routineExercise = null WHERE w.routineExercise.id = :routineExerciseId")
    void detachRoutineExercise(Long routineExerciseId);

    // 유저의 전체 운동 로그 (최신순) — 전체 기록 목록용
    List<WorkoutLog> findAllByUserIdOrderByLoggedAtDesc(Long userId);

    // 특정 종목의 운동 로그 (최신순) — 종목 상세 기록용
    @Query("""
        SELECT wl FROM WorkoutLog wl
        WHERE wl.user.id = :userId
          AND wl.exercise.id = :exerciseId
        ORDER BY wl.loggedAt DESC
        """)
    List<WorkoutLog> findAllByUserIdAndExerciseIdOrderByLoggedAtDesc(
            Long userId, Long exerciseId);
}