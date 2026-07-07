package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.BattleResult;
import com.holyhabit.holyhabit.entity.CharacterStat;
import com.holyhabit.holyhabit.entity.GameCharacter;
import lombok.Getter;

@Getter
public class AdventureConfirmResponse {
    private final String result;
    private final int expGained;
    private final int goldGained;
    private final int levelsGained;
    private final int newLevel;
    private final int newExp;
    private final int requiredExp;
    private final int newAtk;
    private final int newDef;
    private final int newMaxHp;

    // 캐릭터 드롭 정보 (null이면 드롭 없음)
    private final String droppedCharacterName;
    private final String droppedCharacterImageKey;

    public AdventureConfirmResponse(BattleResult result, int expGained,
                                    int goldGained, int levelsGained,
                                    CharacterStat stat,
                                    GameCharacter droppedCharacter) {
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

        // 드롭된 캐릭터 정보 (없으면 null)
        this.droppedCharacterName     = droppedCharacter != null ? droppedCharacter.getName() : null;
        this.droppedCharacterImageKey = droppedCharacter != null ? droppedCharacter.getImageKey() : null;
    }
}