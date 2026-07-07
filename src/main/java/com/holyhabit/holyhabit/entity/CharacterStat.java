package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Random;

@Entity
@Table(name = "character_stats",
        indexes = {
                @Index(
                        name = "idx_character_stats_user_active",
                        columnList = "user_id, is_active"
                )
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class CharacterStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private GameCharacter character;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int exp;

    @Column(nullable = false)
    private int atk;

    @Column(nullable = false)
    private int def;

    @Column(nullable = false)
    private int hp;

    @Column(nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private double doubleAttackChance;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = false;

    private static final int MAX_LEVEL = 20;
    private static final Random random = new Random();

    // 경험치 필요량 공식: level * level * 18
    // Lv1→2: 18, Lv5→6: 450, Lv10→11: 1800, Lv19→20: 6498
    // Lv1→20 총합: 약 36,462 exp (하루 300 exp 기준 약 4개월)
    public int getRequiredExp() {
        return level * level * 18;
    }

    // 경험치 비율 (0.0 ~ 1.0) — Flutter 레벨 바 표시용
    public double getExpProgress() {
        return (double) exp / getRequiredExp();
    }

    // 경험치 추가 + 레벨업 처리
    public int addExpAndLevelUp(int gainExp) {
        this.exp += gainExp;
        int levelsGained = 0;

        while (this.exp >= getRequiredExp() && this.level < MAX_LEVEL) {
            this.exp -= getRequiredExp();
            this.level++;
            applyLevelUpGrowth();
            levelsGained++;
        }

        // 최대 레벨 도달 시 exp 초과분 버림
        if (this.level >= MAX_LEVEL) {
            this.exp = 0;
        }

        return levelsGained;
    }

    // 레벨업 스탯 랜덤 성장
    // bear:   atk +2~+4, def +2~+4, hp +18~+25
    // dragon: atk +3~+6, def +1~+2, hp +10~+18
    // knight: atk +1~+3, def +3~+6, hp +22~+35
    private void applyLevelUpGrowth() {
        String imageKey = this.character.getImageKey();

        switch (imageKey) {
            case "char_dragon" -> {
                this.atk   += 3 + random.nextInt(4);   // +3 ~ +6
                this.def   += 1 + random.nextInt(2);   // +1 ~ +2
                this.maxHp += 10 + random.nextInt(9);  // +10 ~ +18
            }
            case "char_knight" -> {
                this.atk   += 1 + random.nextInt(3);   // +1 ~ +3
                this.def   += 3 + random.nextInt(4);   // +3 ~ +6
                this.maxHp += 22 + random.nextInt(14); // +22 ~ +35
            }
            default -> { // char_bear
                this.atk   += 2 + random.nextInt(3);  // +2 ~ +4
                this.def   += 2 + random.nextInt(3);  // +2 ~ +4
                this.maxHp += 18 + random.nextInt(8); // +18 ~ +25
            }
        }

        // 레벨업 시 HP 전체 회복
        this.hp = this.maxHp;
    }

    public void activate()   { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
}