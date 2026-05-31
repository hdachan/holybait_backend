package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.Battle;
import com.holyhabit.holyhabit.entity.BattleLog;
import com.holyhabit.holyhabit.entity.CharacterStat;
import com.holyhabit.holyhabit.entity.Monster;
import com.holyhabit.holyhabit.service.AdventureService.BattleLogEntry;
import lombok.Getter;

import java.util.List;

@Getter
public class PendingBattleResponse {

    private final Long battleId;
    private final String result;

    private final String monsterName;
    private final int monsterLevel;
    private final int monsterHp;
    private final int monsterAtk;
    private final int monsterDef;
    private final String monsterImageKey;

    private final int playerMaxHp;
    private final int playerAtk;
    private final int playerDef;

    private final List<AdventureStartResponse.TurnLog> logs;

    public PendingBattleResponse(Battle battle, Monster monster,
                                  CharacterStat stat, List<BattleLog> logs) {
        this.battleId = battle.getId();
        this.result = battle.getResult().name();
        this.monsterName = monster.getName();
        this.monsterLevel = monster.getLevel();
        this.monsterHp = monster.getHp();
        this.monsterAtk = monster.getAtk();
        this.monsterDef = monster.getDef();
        this.monsterImageKey = monster.getImageKey();
        this.playerMaxHp = stat.getMaxHp();
        this.playerAtk = stat.getAtk();
        this.playerDef = stat.getDef();
        this.logs = logs.stream()
                .map(bl -> new AdventureStartResponse.TurnLog(
                        new BattleLogEntry(
                                bl.getTurn(), bl.getActor(), bl.getDamage(),
                                bl.isDoubleAttack(), bl.getPlayerHpAfter(),
                                bl.getMonsterHpAfter())))
                .toList();
    }
}
