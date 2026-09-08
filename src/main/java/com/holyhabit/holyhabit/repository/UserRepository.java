package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Provider;
import com.holyhabit.holyhabit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByUuid(String uuid);

    // 신규 유저 여부 판단용
    boolean existsByProviderAndProviderId(Provider provider, String providerId);
}