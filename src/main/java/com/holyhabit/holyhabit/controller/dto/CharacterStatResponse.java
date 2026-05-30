package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.CharacterStat;
import lombok.Getter;

@Getter
public class CharacterStatResponse {
    private final Long statId;
    private final Long characterId;
    private final String characterName;
    private final String imageKey;
    private final boolean isActive;
    private final int level;
    private final int exp;
    private final int requiredExp;
    private final int atk;
    private final int def;
    private final int hp;
    private final int maxHp;

    public CharacterStatResponse(CharacterStat stat) {
        this.statId = stat.getId();
        this.characterId = stat.getCharacter().getId();
        this.characterName = stat.getCharacter().getName();
        this.imageKey = stat.getCharacter().getImageKey();
        this.isActive = stat.isActive();
        this.level = stat.getLevel();
        this.exp = stat.getExp();
        this.requiredExp = stat.getRequiredExp();
        this.atk = stat.getAtk();
        this.def = stat.getDef();
        this.hp = stat.getHp();
        this.maxHp = stat.getMaxHp();
    }
}
