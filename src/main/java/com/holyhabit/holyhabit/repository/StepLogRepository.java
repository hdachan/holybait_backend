package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.StepLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StepLogRepository extends JpaRepository<StepLog, Long> {

    Optional<StepLog> findByUserIdAndLoggedDate(Long userId, LocalDate loggedDate);
}