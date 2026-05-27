package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "character_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class CharacterStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int exp;

    @Column(nullable = false)
    private int atk;

    @Column(nullable = false)
    private int def;

    @Column(nullable = false)
    private int hp;       // 현재 HP (전투 시 max_hp 로 시작)

    @Column(nullable = false)
    private int maxHp;

    // 더블어택 확률 (0.0 ~ 1.0)
    @Column(nullable = false)
    private double doubleAttackChance;

    // ── 레벨업 공식: requiredExp = level * 100 ──
    public int getRequiredExp() {
        return level * 100;
    }

    // 경험치 추가 + 레벨업 처리
    public int addExpAndLevelUp(int gainExp) {
        this.exp += gainExp;
        int levelsGained = 0;
        while (this.exp >= getRequiredExp()) {
            this.exp -= getRequiredExp();
            this.level++;
            this.atk += 2;
            this.def += 1;
            this.maxHp += 10;
            this.hp = this.maxHp; // 레벨업 시 HP 회복
            levelsGained++;
        }
        return levelsGained;
    }
}
