package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.CurrencyLog;
import com.holyhabit.holyhabit.entity.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface CurrencyLogRepository extends JpaRepository<CurrencyLog, Long> {

    // 특정 기간 내 특정 재화 획득 합산 (하루 캡 계산용)
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
}
