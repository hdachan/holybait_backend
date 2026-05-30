package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.CharacterStat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CharacterStatRepository extends JpaRepository<CharacterStat, Long> {

    // 유저의 모든 캐릭터
    List<CharacterStat> findAllByUserId(Long userId);

    // 현재 활성 캐릭터
    Optional<CharacterStat> findByUserIdAndIsActiveTrue(Long userId);

    // 특정 캐릭터 보유 여부
    Optional<CharacterStat> findByUserIdAndCharacterId(Long userId, Long characterId);

    // 모든 캐릭터 비활성화 (캐릭터 변경 시)
    @Modifying
    @Transactional  // 추가
    @Query("UPDATE CharacterStat cs SET cs.isActive = false WHERE cs.user.id = :userId")
    void deactivateAll(Long userId);
}
