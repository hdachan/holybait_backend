package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.UserCurrency;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserCurrencyRepository extends JpaRepository<UserCurrency, Long> {

    Optional<UserCurrency> findByUserId(Long userId);

    // 낙관적 락으로 조회 — 동시 수정 시 OptimisticLockException 발생
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT uc FROM UserCurrency uc WHERE uc.user.id = :userId")
    Optional<UserCurrency> findByUserIdWithLock(Long userId);
}
