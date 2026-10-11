package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.QuestResponse;
import com.holyhabit.holyhabit.entity.Quest;
import com.holyhabit.holyhabit.entity.User;
import com.holyhabit.holyhabit.entity.UserQuest;
import com.holyhabit.holyhabit.repository.QuestRepository;
import com.holyhabit.holyhabit.repository.UserQuestRepository;
import com.holyhabit.holyhabit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    // ── 오늘 날짜 기준 periodKey (daily) ──
    // 예: "2026-09-19"
    private String todayPeriodKey() {
        return LocalDate.now(KST).toString();
    }

    // ── 이번 주 기준 periodKey (weekly, 월요일 시작) ──
    // 예: "2026-W38"
    private String thisWeekPeriodKey() {
        LocalDate today = LocalDate.now(KST);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int week = monday.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = monday.get(IsoFields.WEEK_BASED_YEAR);
        return year + "-W" + week;
    }

    // type에 맞는 현재 periodKey 반환 (tutorial은 null)
    private String currentPeriodKeyFor(String type) {
        return switch (type) {
            case "daily" -> todayPeriodKey();
            case "weekly" -> thisWeekPeriodKey();
            default -> null; // tutorial, achievement 등
        };
    }

    // ── 내 퀘스트 목록 조회 (type별) ──
    @Transactional
    public List<QuestResponse> getMyQuests(Long userId, String type) {
        String periodKey = currentPeriodKeyFor(type);
        initUserQuestsIfNeeded(userId, type, periodKey);

        List<UserQuest> userQuests;
        if (periodKey == null) {
            // tutorial, achievement 등 periodKey 없는 타입
            userQuests = userQuestRepository.findByUserIdAndTypeNoPeriod(userId, type);
        } else {
            userQuests = userQuestRepository
                    .findAllByUserIdAndTypeAndPeriodKey(userId, type, periodKey);
        }

        return userQuests.stream().map(uq -> new QuestResponse(
                uq.getQuest().getId(),
                uq.getQuest().getTitle(),
                uq.getQuest().getDescription(),
                uq.getQuest().getActionType(),
                uq.getProgress(),
                uq.getQuest().getTargetCount(),
                uq.getStatus(),
                uq.getQuest().getRewardGold(),
                uq.getQuest().getRewardExp()
        )).toList();
    }

    // 유저가 처음 조회할 때 (또는 새 주기 시작 시) UserQuest 자동 생성
    @Transactional
    public void initUserQuestsIfNeeded(Long userId, String type, String periodKey) {
        // 유저 행을 먼저 잠가 같은 유저의 동시 요청을 한 줄로 세움
        // (튜토리얼·업적은 periodKey가 NULL이라 MySQL 유니크 제약이 중복을 막지 못함)
        // ⚠️ 잠금은 이 트랜잭션의 첫 조회여야 함 — MySQL 기본 격리 수준(REPEATABLE READ)은
        //    첫 일반 조회 시점의 데이터를 계속 보여주므로, 잠금 전에 조회하면
        //    다른 요청이 방금 만든 행을 못 보고 또 만들 수 있음
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        List<Quest> quests = questRepository.findAllByTypeAndIsActiveTrueOrderBySortOrder(type);

        for (Quest quest : quests) {
            boolean exists = userQuestExists(userId, quest.getId(), periodKey);

            if (!exists) {
                userQuestRepository.save(UserQuest.builder()
                        .user(user)
                        .quest(quest)
                        .periodKey(periodKey)
                        .progress(0)
                        .status("in_progress")
                        .build());
                log.info("userId={} quest={} periodKey={} 퀘스트 초기화",
                        userId, quest.getTitle(), periodKey);
            }
        }
    }

    private boolean userQuestExists(Long userId, Long questId, String periodKey) {
        if (periodKey == null) {
            return userQuestRepository.findByUserIdAndQuestIdNoPeriod(userId, questId).isPresent();
        }
        return userQuestRepository
                .findByUserIdAndQuestIdAndPeriodKey(userId, questId, periodKey).isPresent();
    }

    // ── 특정 행동 발생 시 진행도 갱신 (다른 서비스에서 호출) ──
    @Transactional
    public void progressQuest(Long userId, String actionType) {
        progressQuest(userId, actionType, 1);
    }

    @Transactional
    public void progressQuest(Long userId, String actionType, int amount) {
        // actionType을 가진 퀘스트가 daily/weekly일 수도 있으니
        // 오늘/이번주 periodKey 둘 다 체크해야 함 → null 포함 조회
        // (tutorial은 periodKey null, daily는 오늘, weekly는 이번주)

        // 1) tutorial (periodKey null)
        List<UserQuest> targets = userQuestRepository
                .findInProgressByUserIdAndActionType(userId, actionType, null);
        applyProgress(userId, targets, amount);

        // 2) daily (오늘 periodKey)
        List<UserQuest> dailyTargets = userQuestRepository
                .findInProgressByUserIdAndActionType(userId, actionType, todayPeriodKey());
        applyProgress(userId, dailyTargets, amount);

        // 3) weekly (이번주 periodKey)
        List<UserQuest> weeklyTargets = userQuestRepository
                .findInProgressByUserIdAndActionType(userId, actionType, thisWeekPeriodKey());
        applyProgress(userId, weeklyTargets, amount);
    }

    // ── 누적값 기준 진행도 "설정" (achievement 등, total_steps 같은 값) ──
    // increaseProgress와 달리 현재 값을 그대로 progress에 반영
    @Transactional
    public void setProgressValue(Long userId, String actionType, long currentValue) {
        // achievement는 periodKey가 null
        List<UserQuest> targets = userQuestRepository
                .findInProgressByUserIdAndActionType(userId, actionType, null);

        for (UserQuest uq : targets) {
            int target = uq.getQuest().getTargetCount();
            int newProgress = (int) Math.min(currentValue, target);
            if (newProgress > uq.getProgress()) {
                uq.setProgressValue(newProgress);
                if (uq.isCompleted()) {
                    log.info("userId={} 업적 달성: {}", userId, uq.getQuest().getTitle());
                }
            }
        }
    }

    private void applyProgress(Long userId, List<UserQuest> targets, int amount) {
        for (UserQuest uq : targets) {
            uq.increaseProgress(amount);
            if (uq.isCompleted()) {
                log.info("userId={} 퀘스트 완료: {}", userId, uq.getQuest().getTitle());
            }
        }
    }

    // ── 보상 수령 ──
    @Transactional
    public int claimReward(Long userId, Long questId) {
        UserQuest uq = userQuestRepository
                .findFirstByUserIdAndQuestIdOrderByIdDesc(userId, questId)
                .orElseThrow(() -> new RuntimeException("퀘스트를 찾을 수 없습니다."));

        if (!uq.isCompleted()) {
            throw new RuntimeException("아직 완료되지 않은 퀘스트입니다.");
        }
        if (uq.isClaimed()) {
            throw new RuntimeException("이미 보상을 받았습니다.");
        }

        int gold = uq.getQuest().getRewardGold();
        if (gold > 0) {
            currencyService.addGold(userId, gold, "QUEST", uq.getQuest().getId());
        }

        uq.claim();
        return gold;
    }
}