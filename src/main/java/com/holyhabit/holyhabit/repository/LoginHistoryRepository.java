package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // 스케줄러용 — 보관 기간(3개월)이 지난 접속 로그 삭제
    @Modifying
    @Query("DELETE FROM LoginHistory lh WHERE lh.createdAt < :cutoff")
    int deleteOlderThan(LocalDateTime cutoff);
}
