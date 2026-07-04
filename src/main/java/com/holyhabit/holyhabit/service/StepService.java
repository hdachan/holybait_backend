package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.StepRewardResponse;
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
public class StepService {

    private static final int DAILY_SHOE_COIN_CAP = 20;
    private static final int STEPS_PER_COIN = 1000;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StepLogRepository stepLogRepository;
    private final UserRepository userRepository;
    private final UserCurrencyRepository userCurrencyRepository;
    private final CurrencyLogRepository currencyLogRepository;

    // 걸음 수 보상 받기 (유저가 버튼 누를 때)
    @Transactional
    public StepRewardResponse claimStepReward(Long userId, int totalSteps) {

        LocalDate todayKst = LocalDate.now(KST);
        LocalDateTime from = todayKst.atStartOfDay();
        LocalDateTime to = todayKst.plusDays(1).atStartOfDay();

        // 오늘 총 신발코인 획득량 (운동 + 걸음 합산 — 캡 공유)
        int totalTodayEarned = currencyLogRepository
                .sumAmountByUserAndTypeAndPeriod(userId, CurrencyType.SHOE_COIN, from, to);

        if (totalTodayEarned >= DAILY_SHOE_COIN_CAP) {
            return new StepRewardResponse(0, totalTodayEarned, DAILY_SHOE_COIN_CAP, false);
        }

        // 총 걸음으로 받을 수 있는 코인
        int earnableCoins = totalSteps / STEPS_PER_COIN;

        // 오늘 걸음으로 이미 받은 코인
        int alreadyGrantedByStep = currencyLogRepository
                .sumAmountByUserAndTypeAndSource(userId, CurrencyType.SHOE_COIN,
                        CurrencySource.STEP, from, to);

        // 지금 받을 수 있는 코인
        int canGrant = earnableCoins - alreadyGrantedByStep;
        if (canGrant <= 0) {
            return new StepRewardResponse(0, totalTodayEarned, DAILY_SHOE_COIN_CAP, false);
        }

        // 하루 캡 적용
        int remaining = DAILY_SHOE_COIN_CAP - totalTodayEarned;
        int grant = Math.min(canGrant, remaining);
        if (grant <= 0) {
            return new StepRewardResponse(0, totalTodayEarned, DAILY_SHOE_COIN_CAP, false);
        }

        // 코인 지급
        UserCurrency currency = userCurrencyRepository
                .findByUserIdWithLock(userId)
                .orElseGet(() -> createCurrency(userId));
        currency.addShoeCoin(grant);

        CurrencyLog currencyLog = CurrencyLog.builder()
                .user(currency.getUser())
                .currencyType(CurrencyType.SHOE_COIN)
                .amount(grant)
                .source(CurrencySource.STEP)
                .build();
        currencyLogRepository.save(currencyLog);

        log.info("userId={} 걸음 신발코인 {}개 지급 (총걸음={}, 오늘총={})",
                userId, grant, totalSteps, totalTodayEarned + grant);

        return new StepRewardResponse(grant, totalTodayEarned + grant, DAILY_SHOE_COIN_CAP, true);
    }

    // 걸음 수 저장 (자정 — 다음날 앱 켤 때 어제 걸음 수 전송)
    @Transactional
    public void saveStepLog(Long userId, int stepCount, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        stepLogRepository.findByUserIdAndLoggedDate(userId, date)
                .ifPresentOrElse(
                        // 이미 있으면 업데이트 (더 많이 걸은 경우 대비)
                        existing -> existing.updateStepCount(stepCount),
                        // 없으면 새로 저장
                        () -> stepLogRepository.save(StepLog.builder()
                                .user(user)
                                .stepCount(stepCount)
                                .loggedDate(date)
                                .build())
                );

        log.info("userId={} 걸음 수 저장 date={} steps={}", userId, date, stepCount);
    }

    private UserCurrency createCurrency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return userCurrencyRepository.save(UserCurrency.builder()
                .user(user).gold(0).shoeCoin(0).build());
    }
}