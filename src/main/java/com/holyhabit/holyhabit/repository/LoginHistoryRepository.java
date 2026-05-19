package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
