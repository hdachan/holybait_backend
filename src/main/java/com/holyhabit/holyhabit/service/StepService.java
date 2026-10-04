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
    private static final int MAX_DAILY_STEPS = 100_000;      // 하루 최대 인정 걸음
    private static final int MAX_STEP_LOG_DAYS_AGO = 3;      // 며칠 전 날짜까지 저장 허용
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StepLogRepository stepLogRepository;
    private final UserRepository userRepository;
    private final UserCurrencyRepository userCurrencyRepository;
    private final CurrencyLogRepository currencyLogRepository;
    private final QuestService questService;

    // 걸음 수 보상 받기 (유저가 버튼 누를 때)
    @Transactional
    public StepRewardResponse claimStepReward(Long userId, int rawTotalSteps) {
        int totalSteps = Math.max(0, Math.min(rawTotalSteps, MAX_DAILY_STEPS));

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
    // 걸음은 폰에서 오는 값이라 진위를 증명할 수 없음 → 상식 범위만 허용
    // 범위 밖 날짜는 에러 대신 저장하지 않고 넘김 (앱이 같은 날짜를 계속 재전송하지 않도록)
    @Transactional
    public void saveStepLog(Long userId, int rawStepCount, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        LocalDate todayKst = LocalDate.now(KST);
        if (date == null
                || date.isAfter(todayKst)                                  // 미래 날짜
                || date.isBefore(todayKst.minusDays(MAX_STEP_LOG_DAYS_AGO)) // 너무 오래된 날짜
                || date.isBefore(user.getCreatedAt().toLocalDate())) {     // 가입 전 날짜
            log.warn("userId={} 걸음 저장 무시 — 허용 범위 밖 날짜 date={} steps={}",
                    userId, date, rawStepCount);
            return;
        }

        // 하루 최대 걸음까지만 인정 (넘는 값은 거부하지 않고 상한으로 맞춤)
        int stepCount = Math.max(0, Math.min(rawStepCount, MAX_DAILY_STEPS));
        if (stepCount != rawStepCount) {
            log.warn("userId={} 걸음 수 상한 적용 date={} {} → {}",
                    userId, date, rawStepCount, stepCount);
        }

        stepLogRepository.findByUserIdAndLoggedDate(userId, date)
                .ifPresentOrElse(
                        // 이미 있으면 업데이트 (더 많이 걸은 경우 대비, 차이만큼 누적치 반영)
                        existing -> {
                            int previous = existing.getStepCount();
                            if (stepCount > previous) {
                                long delta = stepCount - previous;
                                existing.updateStepCount(stepCount);
                                user.addTotalSteps(delta);
                                log.info("userId={} 누적걸음수 +{} (총 {})",
                                        userId, delta, user.getTotalSteps());
                            }
                        },
                        // 없으면 새로 저장 → 전체 stepCount만큼 누적치 증가
                        () -> {
                            stepLogRepository.save(StepLog.builder()
                                    .user(user)
                                    .stepCount(stepCount)
                                    .loggedDate(date)
                                    .build());
                            user.addTotalSteps(stepCount);
                            log.info("userId={} 누적걸음수 +{} (총 {})",
                                    userId, stepCount, user.getTotalSteps());
                        }
                );

        // 누적 걸음수 기준 achievement 퀘스트 진행도 갱신
        questService.setProgressValue(userId, "total_steps", user.getTotalSteps());

        log.info("userId={} 걸음 수 저장 date={} steps={}", userId, date, stepCount);
    }

    private UserCurrency createCurrency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return userCurrencyRepository.save(UserCurrency.builder()
                .user(user).gold(0).shoeCoin(0).build());
    }
}