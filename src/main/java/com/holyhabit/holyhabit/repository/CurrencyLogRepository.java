package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.CurrencyLog;
import com.holyhabit.holyhabit.entity.CurrencySource;
import com.holyhabit.holyhabit.entity.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface CurrencyLogRepository extends JpaRepository<CurrencyLog, Long> {

    // 기간 내 특정 재화 획득 합산 (하루 캡 계산용 — 운동 + 걸음 합산)
    @Query("""
        SELECT COALESCE(SUM(cl.amount), 0)
        FROM CurrencyLog cl
        WHERE cl.user.id = :userId
          AND cl.currencyType = :currencyType
          AND cl.amount > 0
          AND cl.createdAt >= :from
          AND cl.createdAt < :to
        """)
    int sumAmountByUserAndTypeAndPeriod(
            Long userId,
            CurrencyType currencyType,
            LocalDateTime from,
            LocalDateTime to
    );

    // 오늘 특정 운동(routineExercise)에서 이미 받은 코인 합산
    @Query("""
        SELECT COALESCE(SUM(cl.amount), 0)
        FROM CurrencyLog cl
        JOIN WorkoutLog wl ON wl.id = cl.referenceId
        WHERE cl.user.id = :userId
          AND cl.currencyType = :currencyType
          AND wl.routineExercise.id = :routineExerciseId
          AND cl.amount > 0
          AND cl.createdAt >= :from
          AND cl.createdAt < :to
        """)
    int sumAmountByUserAndTypeAndReferenceRoutineExercise(
            Long userId,
            CurrencyType currencyType,
            Long routineExerciseId,
            LocalDateTime from,
            LocalDateTime to
    );

    // 오늘 걸음(STEP)으로 이미 받은 코인 합산 — 중복 지급 방지용
    @Query("""
        SELECT COALESCE(SUM(cl.amount), 0)
        FROM CurrencyLog cl
        WHERE cl.user.id = :userId
          AND cl.currencyType = :currencyType
          AND cl.source = :source
          AND cl.amount > 0
          AND cl.createdAt >= :from
          AND cl.createdAt < :to
        """)
    int sumAmountByUserAndTypeAndSource(
            Long userId,
            CurrencyType currencyType,
            CurrencySource source,
            LocalDateTime from,
            LocalDateTime to
    );
}