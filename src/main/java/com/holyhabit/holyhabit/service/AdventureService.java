package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.AdventureStartResponse;
import com.holyhabit.holyhabit.controller.dto.AdventureConfirmResponse;
import com.holyhabit.holyhabit.controller.dto.PendingBattleResponse;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdventureService {

    private final StageRepository stageRepository;
    private final MonsterRepository monsterRepository;
    private final CharacterRepository characterRepository;
    private final CharacterStatRepository characterStatRepository;
    private final BattleRepository battleRepository;
    private final BattleLogRepository battleLogRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    private static final Random random = new Random();

    // 배틀 승리 시 캐릭터 드롭 확률 5%
    private static final double CHARACTER_DROP_CHANCE = 0.05;

    @Transactional(readOnly = true)
    public List<Stage> getStages() {
        return stageRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public List<CharacterStat> getAllStats(Long userId) {
        List<CharacterStat> stats = characterStatRepository.findAllByUserId(userId);
        if (stats.isEmpty()) {
            try {
                stats = createAllDefaultStats(userId);
            } catch (Exception e) {
                stats = characterStatRepository.findAllByUserId(userId);
            }
        }
        return stats;
    }

    @Transactional
    public CharacterStat getActiveStat(Long userId) {
        return characterStatRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseGet(() -> getAllStats(userId).get(0));
    }

    @Transactional
    public CharacterStat selectCharacter(Long userId, Long characterStatId) {
        CharacterStat target = characterStatRepository.findById(characterStatId)
                .orElseThrow(() -> new RuntimeException("캐릭터를 찾을 수 없습니다."));
        if (!target.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인 캐릭터가 아닙니다.");
        }
        characterStatRepository.deactivateAll(userId);
        target.activate();
        return target;
    }

    // 미수령 배틀 조회
    @Transactional(readOnly = true)
    public Optional<PendingBattleResponse> getPendingBattle(Long userId) {
        return battleRepository
                .findTopByUserIdAndRewardsClaimedFalseOrderByCreatedAtDesc(userId)
                .map(battle -> {
                    Monster monster = battle.getMonster();
                    CharacterStat stat = getActiveStat(userId);
                    List<BattleLog> logs =
                            battleLogRepository.findAllByBattleIdOrderByTurnAsc(battle.getId());
                    return new PendingBattleResponse(battle, monster, stat, logs);
                });
    }

    // 배틀 포기
    @Transactional
    public void abandonBattle(Long userId, Long battleId) {
        Battle battle = battleRepository.findByIdAndUserId(battleId, userId)
                .orElseThrow(() -> new RuntimeException("배틀을 찾을 수 없습니다."));
        battleLogRepository.deleteAllByBattleId(battleId);
        battleRepository.delete(battle);
        log.info("userId={} battleId={} 배틀 포기", userId, battleId);
    }

    // 모험 시작
    @Transactional
    public AdventureStartResponse startBattle(Long userId, Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("스테이지를 찾을 수 없습니다."));

        CharacterStat stat = getActiveStat(userId);

        if (stat.getLevel() < stage.getMinLevel()) {
            throw new RuntimeException("레벨이 부족합니다. (필요: Lv" + stage.getMinLevel() + ")");
        }

        var currency = currencyService.getCurrency(userId);
        if (currency.getShoeCoin() < stage.getShoeCoinCost()) {
            throw new RuntimeException("신발코인이 부족합니다.");
        }
        currencyService.spendShoeCoin(userId, stage.getShoeCoinCost());

        Monster monster = monsterRepository.findRandomByStageId(stageId)
                .orElseThrow(() -> new RuntimeException("몬스터를 찾을 수 없습니다."));

        BattleCalcResult calc = calculateBattle(stat, monster);

        Battle battle = Battle.builder()
                .user(userRepository.getReferenceById(userId))
                .monster(monster)
                .result(calc.result())
                .build();
        battleRepository.save(battle);

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

    // 보상 수령
    @Transactional
    public AdventureConfirmResponse confirmRewards(Long userId, Long battleId) {
        Battle battle = battleRepository.findByIdAndUserId(battleId, userId)
                .orElseThrow(() -> new RuntimeException("배틀을 찾을 수 없습니다."));

        if (battle.isRewardsClaimed()) {
            throw new RuntimeException("이미 보상을 수령했습니다.");
        }

        battle.claimRewards();

        CharacterStat stat = getActiveStat(userId);
        int expGained = 0;
        int goldGained = 0;
        int levelsGained = 0;
        GameCharacter droppedCharacter = null;

        if (battle.getResult() == BattleResult.WIN) {
            Monster monster = battle.getMonster();
            expGained = monster.getExpReward();
            goldGained = monster.getGoldReward();
            levelsGained = stat.addExpAndLevelUp(expGained);
            currencyService.grantGold(userId, goldGained,
                    CurrencySource.BATTLE, battle.getId());

            // 캐릭터 드롭 — 몬스터에 지정된 캐릭터 5% 확률
            droppedCharacter = tryDropCharacter(userId, battle.getMonster());
            if (droppedCharacter != null) {
                log.info("userId={} 캐릭터 드롭 발생: characterId={}",
                        userId, droppedCharacter.getId());
            }
        }

        // 보상 수령 완료 → battle_logs 삭제
        battleLogRepository.deleteAllByBattleId(battleId);
        log.info("userId={} battleId={} 보상 수령 완료, 로그 삭제", userId, battleId);

        return new AdventureConfirmResponse(
                battle.getResult(), expGained, goldGained,
                levelsGained, stat, droppedCharacter);
    }

    // 캐릭터 드롭 처리
    // 몬스터에 dropCharacter 지정된 경우 dropChance 확률로 드롭
    // dropCharacter가 null이면 드롭 없음
    private GameCharacter tryDropCharacter(Long userId, Monster monster) {
        // 드롭 캐릭터 미지정 몬스터 → 드롭 없음
        if (monster.getDropCharacter() == null) return null;

        // 몬스터별 드롭 확률 체크
        if (random.nextDouble() >= monster.getDropChance()) return null;

        GameCharacter dropped = monster.getDropCharacter();

        // CharacterStat 새로 생성 (중복 보유 가능)
        User user = userRepository.getReferenceById(userId);
        CharacterStat newStat = CharacterStat.builder()
                .user(user)
                .character(dropped)
                .level(1).exp(0)
                .atk(dropped.getBaseAtk())
                .def(dropped.getBaseDef())
                .hp(dropped.getBaseHp())
                .maxHp(dropped.getBaseHp())
                .doubleAttackChance(dropped.getDoubleAttackChance())
                .isActive(false)
                .build();
        characterStatRepository.save(newStat);

        return dropped;
    }

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

        return new BattleCalcResult(
                monsterHp <= 0 ? BattleResult.WIN : BattleResult.LOSE, logs);
    }

    private List<CharacterStat> createAllDefaultStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        List<GameCharacter> allCharacters = characterRepository.findAllByOrderByIdAsc();
        if (allCharacters.isEmpty()) {
            throw new RuntimeException("기본 캐릭터가 없습니다.");
        }

        List<CharacterStat> result = new ArrayList<>();
        for (int i = 0; i < allCharacters.size(); i++) {
            GameCharacter gc = allCharacters.get(i);
            CharacterStat stat = CharacterStat.builder()
                    .user(user)
                    .character(gc)
                    .level(1).exp(0)
                    .atk(gc.getBaseAtk())
                    .def(gc.getBaseDef())
                    .hp(gc.getBaseHp())
                    .maxHp(gc.getBaseHp())
                    .doubleAttackChance(gc.getDoubleAttackChance())
                    .isActive(i == 0)
                    .build();
            result.add(characterStatRepository.save(stat));
        }
        return result;
    }

    public record BattleLogEntry(
            int turn, String actor, int damage,
            boolean isDoubleAttack, int playerHpAfter, int monsterHpAfter) {}

    private record BattleCalcResult(BattleResult result, List<BattleLogEntry> logs) {}
}