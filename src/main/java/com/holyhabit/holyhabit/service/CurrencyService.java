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
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSetRepository workoutSetRepository;

    // 재화 조회
    @Transactional(readOnly = true)
    public CurrencyResponse getCurrency(Long userId) {
        UserCurrency currency = getOrCreate(userId);
        int todayShoeCoin = getTodayShoeCoin(userId);
        return new CurrencyResponse(
                currency.getGold(),
                currency.getShoeCoin(),
                todayShoeCoin,
                DAILY_SHOE_COIN_CAP
        );
    }

    // 신발 코인 지급
    @Transactional
    public int grantShoeCoin(Long userId, int currentSetCount,
                              Long workoutLogId, Long routineExerciseId) {

        LocalDate todayKst = LocalDate.now(KST);
        LocalDateTime from = todayKst.atStartOfDay();
        LocalDateTime to = todayKst.plusDays(1).atStartOfDay();

        // 오늘 전체 신발코인 획득량 (하루 캡 계산)
        int totalTodayEarned = currencyLogRepository
                .sumAmountByUserAndTypeAndPeriod(userId, CurrencyType.SHOE_COIN, from, to);

        if (totalTodayEarned >= DAILY_SHOE_COIN_CAP) {
            log.debug("userId={} 오늘 신발코인 캡 도달", userId);
            return 0;
        }

        // 오늘 이 운동에서 이미 받은 코인 수
        // = 오늘 이 routineExercise 의 workout_logs 에서 currency_logs 를 통해 계산
        int alreadyGrantedForThisExercise = currencyLogRepository
                .sumAmountByUserAndTypeAndReferenceRoutineExercise(
                        userId, CurrencyType.SHOE_COIN, routineExerciseId, from, to);

        // 새로 지급할 양 = 현재 세트 수 - 이미 받은 양
        int newGrant = currentSetCount - alreadyGrantedForThisExercise;
        if (newGrant <= 0) {
            log.debug("userId={} routineExerciseId={} 새 세트 없음 (current={}, alreadyGranted={})",
                    userId, routineExerciseId, currentSetCount, alreadyGrantedForThisExercise);
            return 0;
        }

        // 하루 캡 제한 적용
        int remaining = DAILY_SHOE_COIN_CAP - totalTodayEarned;
        int grant = Math.min(newGrant, remaining);
        if (grant <= 0) return 0;

        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseGet(() -> createCurrency(userId));

        currency.addShoeCoin(grant);

        CurrencyLog currencyLog = CurrencyLog.builder()
                .user(currency.getUser())
                .currencyType(CurrencyType.SHOE_COIN)
                .amount(grant)
                .source(CurrencySource.WORKOUT)
                .referenceId(workoutLogId)
                .build();
        currencyLogRepository.save(currencyLog);

        log.info("userId={} 신발코인 {}개 지급 (오늘총={})", userId, grant, totalTodayEarned + grant);
        return grant;
    }

    public int getTodayShoeCoin(Long userId) {
        LocalDate todayKst = LocalDate.now(KST);
        LocalDateTime from = todayKst.atStartOfDay();
        LocalDateTime to = todayKst.plusDays(1).atStartOfDay();
        return currencyLogRepository.sumAmountByUserAndTypeAndPeriod(
                userId, CurrencyType.SHOE_COIN, from, to);
    }

    private UserCurrency getOrCreate(Long userId) {
        return userCurrencyRepository.findByUserId(userId)
                .orElseGet(() -> createCurrency(userId));
    }

    private UserCurrency createCurrency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return userCurrencyRepository.save(UserCurrency.builder()
                .user(user).gold(0).shoeCoin(0).build());
    }


    // 신발코인 소모 (모험 입장 시)
    @Transactional
    public void spendShoeCoin(Long userId, int amount) {
        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseGet(() -> createCurrency(userId));

        if (currency.getShoeCoin() < amount) {
            throw new RuntimeException("신발코인이 부족합니다.");
        }

        // shoeCoin 차감 — 음수로 currency_logs 기록
        // UserCurrency 에 spendShoeCoin 메서드 추가 필요
        currency.spendShoeCoin(amount);

        CurrencyLog log = CurrencyLog.builder()
                .user(currency.getUser())
                .currencyType(CurrencyType.SHOE_COIN)
                .amount(-amount)           // 음수 = 소비
                .source(CurrencySource.SPEND)
                .build();
        currencyLogRepository.save(log);
    }


    @Transactional
    public void grantGold(Long userId, int amount, CurrencySource source, Long referenceId) {
        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseGet(() -> createCurrency(userId));

        currency.addGold(amount);

        CurrencyLog log = CurrencyLog.builder()
                .user(currency.getUser())
                .currencyType(CurrencyType.GOLD)
                .amount(amount)
                .source(source)
                .referenceId(referenceId)
                .build();
        currencyLogRepository.save(log);
    }

}
