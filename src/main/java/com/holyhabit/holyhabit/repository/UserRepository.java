package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Provider;
import com.holyhabit.holyhabit.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByUuid(String uuid);

    // 신규 유저 여부 판단용
    boolean existsByProviderAndProviderId(Provider provider, String providerId);

    // 유저 행 잠금 (SELECT ... FOR UPDATE) — 같은 유저의 동시 처리를 한 줄로 세울 때
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(Long id);
}