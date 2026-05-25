package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.CurrencyResponse;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final int DAILY_SHOE_COIN_CAP = 20;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserCurrencyRepository userCurrencyRepository;
    private final CurrencyLogRepository currencyLogRepository;
    private final UserRepository userRepository;

    // 재화 조회
    @Transactional(readOnly = true)
    public CurrencyResponse getCurrency(Long userId) {
        UserCurrency currency = getOrCreate(userId);
        return new CurrencyResponse(currency.getGold(), currency.getShoeCoin());
    }

    // 신발 코인 지급 (운동 저장 시 호출)
    @Transactional
    public void grantShoeCoin(Long userId, int setCount, Long workoutLogId) {
        // KST 기준 오늘 00:00 ~ 내일 00:00
        LocalDate todayKst = LocalDate.now(KST);
        LocalDateTime from = todayKst.atStartOfDay();
        LocalDateTime to = todayKst.plusDays(1).atStartOfDay();

        // 오늘 이미 받은 신발 코인 합산
        int todayEarned = currencyLogRepository.sumAmountByUserAndTypeAndPeriod(
                userId, CurrencyType.SHOE_COIN, from, to);

        int remaining = DAILY_SHOE_COIN_CAP - todayEarned;
        if (remaining <= 0) {
            log.debug("userId={} 오늘 신발코인 캡 도달 ({}개)", userId, DAILY_SHOE_COIN_CAP);
            return;
        }

        int grant = Math.min(setCount, remaining);
        if (grant <= 0) return;

        // 낙관적 락으로 UserCurrency 업데이트
        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseGet(() -> createCurrency(userId));

        currency.addShoeCoin(grant);

        // 로그 기록
        User user = currency.getUser();
        CurrencyLog currencyLog = CurrencyLog.builder()
                .user(user)
                .currencyType(CurrencyType.SHOE_COIN)
                .amount(grant)
                .source(CurrencySource.WORKOUT)
                .referenceId(workoutLogId)
                .build();
        currencyLogRepository.save(currencyLog);

        log.info("userId={} 신발코인 {}개 지급 (오늘총={})", userId, grant, todayEarned + grant);
    }

    // 골드 지급
    @Transactional
    public void grantGold(Long userId, int amount, CurrencySource source, Long referenceId) {
        if (amount <= 0) return;

        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseGet(() -> createCurrency(userId));

        currency.addGold(amount);

        CurrencyLog currencyLog = CurrencyLog.builder()
                .user(currency.getUser())
                .currencyType(CurrencyType.GOLD)
                .amount(amount)
                .source(source)
                .referenceId(referenceId)
                .build();
        currencyLogRepository.save(currencyLog);
    }

    // UserCurrency 없으면 생성
    private UserCurrency getOrCreate(Long userId) {
        return userCurrencyRepository.findByUserId(userId)
                .orElseGet(() -> createCurrency(userId));
    }

    private UserCurrency createCurrency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        UserCurrency currency = UserCurrency.builder()
                .user(user)
                .gold(0)
                .shoeCoin(0)
                .build();
        return userCurrencyRepository.save(currency);
    }
}
