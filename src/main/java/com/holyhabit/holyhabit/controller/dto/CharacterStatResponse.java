package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.holyhabit.holyhabit.entity.CharacterStat;

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

    public Long getStatId() { return statId; }
    public Long getCharacterId() { return characterId; }
    public String getCharacterName() { return characterName; }
    public String getImageKey() { return imageKey; }

    // isActive → Jackson 이 "active" 로 직렬화하는 걸 방지
    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }

    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getRequiredExp() { return requiredExp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
}