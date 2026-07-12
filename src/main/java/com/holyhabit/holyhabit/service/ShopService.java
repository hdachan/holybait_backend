package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.ShopGachaResponse;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private static final int GACHA_COST = 1400; // 뽑기 1회 비용
    private static final Random random = new Random();

    private final UserRepository userRepository;
    private final UserCurrencyRepository userCurrencyRepository;
    private final CurrencyLogRepository currencyLogRepository;
    private final CharacterRepository characterRepository;
    private final CharacterStatRepository characterStatRepository;

    // 캐릭터 뽑기
    @Transactional
    public ShopGachaResponse gacha(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 슬롯 꽉 찬 경우 뽑기 불가
        int currentCount = characterStatRepository.countByUserId(userId);
        if (currentCount >= user.getSlotCount()) {
            throw new RuntimeException("슬롯이 꽉 찼습니다. 슬롯을 확장해주세요.");
        }

        // 골드 차감
        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("재화 정보를 찾을 수 없습니다."));

        if (currency.getGold() < GACHA_COST) {
            throw new RuntimeException("골드가 부족합니다.");
        }
        currency.spendGold(GACHA_COST);

        // 골드 소모 로그
        currencyLogRepository.save(CurrencyLog.builder()
                .user(user)
                .currencyType(CurrencyType.GOLD)
                .amount(-GACHA_COST)
                .source(CurrencySource.SPEND)
                .build());

        // 전체 캐릭터 중 랜덤 1개 (중복 가능)
        List<GameCharacter> allCharacters = characterRepository.findAllByOrderByIdAsc();
        if (allCharacters.isEmpty()) {
            throw new RuntimeException("캐릭터 데이터가 없습니다.");
        }
        GameCharacter picked = allCharacters.get(random.nextInt(allCharacters.size()));

        // CharacterStat 생성
        CharacterStat newStat = CharacterStat.builder()
                .user(user)
                .character(picked)
                .level(1).exp(0)
                .atk(picked.getBaseAtk())
                .def(picked.getBaseDef())
                .hp(picked.getBaseHp())
                .maxHp(picked.getBaseHp())
                .doubleAttackChance(picked.getDoubleAttackChance())
                .isActive(false)
                .build();
        characterStatRepository.save(newStat);

        log.info("userId={} 뽑기 결과: {} (골드 {}개 소모)",
                userId, picked.getName(), GACHA_COST);

        return new ShopGachaResponse(
                picked.getName(),
                picked.getImageKey(),
                currency.getGold(),
                (int) (long) newStat.getId()
        );
    }
}