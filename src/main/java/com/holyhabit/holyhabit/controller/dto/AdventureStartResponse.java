package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.Battle;
import com.holyhabit.holyhabit.entity.CharacterStat;
import com.holyhabit.holyhabit.entity.Monster;
import com.holyhabit.holyhabit.service.AdventureService.BattleLogEntry;
import lombok.Getter;

import java.util.List;

@Getter
public class AdventureStartResponse {

    private final Long battleId;
    private final String result;       // WIN / LOSE

    // 몬스터 정보
    private final Long monsterId;
    private final String monsterName;
    private final int monsterLevel;
    private final int monsterHp;
    private final int monsterAtk;
    private final int monsterDef;
    private final String monsterImageKey;

    // 플레이어 정보 (배틀 시작 시)
    private final int playerMaxHp;
    private final int playerAtk;
    private final int playerDef;

    // 전체 턴 로그 — Flutter 가 순서대로 재생
    private final List<TurnLog> logs;

    public AdventureStartResponse(Battle battle, Monster monster,
                                   CharacterStat stat, List<BattleLogEntry> entries) {
        this.battleId = battle.getId();
        this.result = battle.getResult().name();
        this.monsterId = monster.getId();
        this.monsterName = monster.getName();
        this.monsterLevel = monster.getLevel();
        this.monsterHp = monster.getHp();
        this.monsterAtk = monster.getAtk();
        this.monsterDef = monster.getDef();
        this.monsterImageKey = monster.getImageKey();
        this.playerMaxHp = stat.getMaxHp();
        this.playerAtk = stat.getAtk();
        this.playerDef = stat.getDef();
        this.logs = entries.stream().map(TurnLog::new).toList();
    }

    @Getter
    public static class TurnLog {
        private final int turn;
        private final String actor;          // PLAYER / MONSTER
        private final int damage;
        private final boolean isDoubleAttack;
        private final int playerHpAfter;
        private final int monsterHpAfter;

        public TurnLog(BattleLogEntry e) {
            this.turn = e.turn();
            this.actor = e.actor();
            this.damage = e.damage();
            this.isDoubleAttack = e.isDoubleAttack();
            this.playerHpAfter = e.playerHpAfter();
            this.monsterHpAfter = e.monsterHpAfter();
        }
    }
}
