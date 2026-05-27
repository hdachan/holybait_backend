package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.AdventureStartResponse;
import com.holyhabit.holyhabit.controller.dto.AdventureConfirmResponse;
import com.holyhabit.holyhabit.controller.dto.CurrencyResponse;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdventureService {

    private final StageRepository stageRepository;
    private final MonsterRepository monsterRepository;
    private final CharacterStatRepository characterStatRepository;
    private final BattleRepository battleRepository;
    private final BattleLogRepository battleLogRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    private static final Random random = new Random();

    // 맵 목록 조회
    @Transactional(readOnly = true)
    public List<Stage> getStages() {
        return stageRepository.findAllByOrderByIdAsc();
    }

    // 캐릭터 스탯 조회 (없으면 기본값으로 생성)
    @Transactional
    public CharacterStat getOrCreateStat(Long userId) {
        return characterStatRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStat(userId));
    }

    // 모험 시작
    // 1. 신발코인 소모
    // 2. 랜덤 몬스터 선택
    // 3. 배틀 전체 턴 미리 계산
    // 4. DB 저장
    // 5. 결과 반환 (Flutter가 순서대로 재생)
    @Transactional
    public AdventureStartResponse startBattle(Long userId, Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("스테이지를 찾을 수 없습니다."));

        CharacterStat stat = getOrCreateStat(userId);

        // 레벨 체크
        if (stat.getLevel() < stage.getMinLevel()) {
            throw new RuntimeException("레벨이 부족합니다. (필요: Lv" + stage.getMinLevel() + ")");
        }

        // 신발코인 소모 — 입장 시 즉시 차감
        int remaining = currencyService.getTodayShoeCoin(userId);
        CurrencyResponse currency = currencyService.getCurrency(userId);
        if (currency.getShoeCoin() < stage.getShoeCoinCost()) {
            throw new RuntimeException("신발코인이 부족합니다.");
        }
        // UserCurrency에서 shoeCoin 차감
        currencyService.spendShoeCoin(userId, stage.getShoeCoinCost());

        // 랜덤 몬스터 선택
        Monster monster = monsterRepository.findRandomByStageId(stageId)
                .orElseThrow(() -> new RuntimeException("몬스터를 찾을 수 없습니다."));

        // 배틀 계산
        BattleCalcResult calc = calculateBattle(stat, monster);

        // DB 저장
        Battle battle = Battle.builder()
                .user(userRepository.getReferenceById(userId))
                .monster(monster)
                .result(calc.result())
                .build();
        battleRepository.save(battle);

        // 배틀 로그 저장
        for (BattleLogEntry entry : calc.logs()) {
            battleLogRepository.save(BattleLog.builder()
                    .battle(battle)
                    .turn(entry.turn())
                    .actor(entry.actor())
                    .damage(entry.damage())
                    .isDoubleAttack(entry.isDoubleAttack())
                    .playerHpAfter(entry.playerHpAfter())
                    .monsterHpAfter(entry.monsterHpAfter())
                    .build());
        }

        return new AdventureStartResponse(battle, monster, stat, calc.logs());
    }

    // 보상 수령 확인 (마지막 버튼)
    @Transactional
    public AdventureConfirmResponse confirmRewards(Long userId, Long battleId) {
        Battle battle = battleRepository.findByIdAndUserId(battleId, userId)
                .orElseThrow(() -> new RuntimeException("배틀을 찾을 수 없습니다."));

        if (battle.isRewardsClaimed()) {
            throw new RuntimeException("이미 보상을 수령했습니다.");
        }

        battle.claimRewards();

        CharacterStat stat = getOrCreateStat(userId);
        int expGained = 0;
        int goldGained = 0;
        int levelsGained = 0;

        if (battle.getResult() == BattleResult.WIN) {
            Monster monster = battle.getMonster();
            expGained = monster.getExpReward();
            goldGained = monster.getGoldReward();

            // 경험치 추가 + 레벨업
            levelsGained = stat.addExpAndLevelUp(expGained);

            // 골드 지급
            currencyService.grantGold(userId, goldGained,
                    CurrencySource.BATTLE, battle.getId());
        }

        return new AdventureConfirmResponse(
                battle.getResult(),
                expGained,
                goldGained,
                levelsGained,
                stat
        );
    }

    // ── 배틀 계산 ──
    private BattleCalcResult calculateBattle(CharacterStat player, Monster monster) {
        int playerHp = player.getMaxHp();
        int monsterHp = monster.getHp();
        List<BattleLogEntry> logs = new ArrayList<>();
        int turn = 0;

        while (playerHp > 0 && monsterHp > 0) {
            turn++;

            // 플레이어 공격
            boolean playerDouble = random.nextDouble() < player.getDoubleAttackChance();
            int playerDmg = Math.max(1, player.getAtk() - monster.getDef());
            if (playerDouble) playerDmg *= 2;
            monsterHp = Math.max(0, monsterHp - playerDmg);

            logs.add(new BattleLogEntry(turn, "PLAYER", playerDmg,
                    playerDouble, playerHp, monsterHp));

            if (monsterHp <= 0) break;

            // 몬스터 공격
            boolean monsterDouble = random.nextDouble() < monster.getDoubleAttackChance();
            int monsterDmg = Math.max(1, monster.getAtk() - player.getDef());
            if (monsterDouble) monsterDmg *= 2;
            playerHp = Math.max(0, playerHp - monsterDmg);

            logs.add(new BattleLogEntry(turn, "MONSTER", monsterDmg,
                    monsterDouble, playerHp, monsterHp));
        }

        BattleResult result = monsterHp <= 0 ? BattleResult.WIN : BattleResult.LOSE;
        return new BattleCalcResult(result, logs);
    }

    private CharacterStat createDefaultStat(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        CharacterStat stat = CharacterStat.builder()
                .user(user)
                .level(1).exp(0)
                .atk(15).def(5).hp(100).maxHp(100)
                .doubleAttackChance(0.15)
                .build();
        return characterStatRepository.save(stat);
    }

    public record BattleLogEntry(
            int turn, String actor, int damage,
            boolean isDoubleAttack, int playerHpAfter, int monsterHpAfter) {}

    private record BattleCalcResult(BattleResult result, List<BattleLogEntry> logs) {}
}
