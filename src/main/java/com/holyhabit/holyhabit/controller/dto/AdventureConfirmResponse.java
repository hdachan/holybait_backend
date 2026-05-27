package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.BattleResult;
import com.holyhabit.holyhabit.entity.CharacterStat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdventureConfirmResponse {
    private final String result;       // WIN / LOSE
    private final int expGained;
    private final int goldGained;
    private final int levelsGained;    // 레벨업 횟수
    private final int newLevel;
    private final int newExp;
    private final int requiredExp;     // 다음 레벨까지 필요 경험치
    private final int newAtk;
    private final int newDef;
    private final int newMaxHp;

    public AdventureConfirmResponse(BattleResult result, int expGained,
                                     int goldGained, int levelsGained,
                                     CharacterStat stat) {
        this.result = result.name();
        this.expGained = expGained;
        this.goldGained = goldGained;
        this.levelsGained = levelsGained;
        this.newLevel = stat.getLevel();
        this.newExp = stat.getExp();
        this.requiredExp = stat.getRequiredExp();
        this.newAtk = stat.getAtk();
        this.newDef = stat.getDef();
        this.newMaxHp = stat.getMaxHp();
    }
}
