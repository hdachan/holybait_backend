package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "character_stats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "character_id"}))
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
    private GameCharacter character;   // java.lang.Character 충돌 방지

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

    public int getRequiredExp() {
        return level * 100;
    }

    public int addExpAndLevelUp(int gainExp) {
        this.exp += gainExp;
        int levelsGained = 0;
        while (this.exp >= getRequiredExp()) {
            this.exp -= getRequiredExp();
            this.level++;
            this.atk += 2;
            this.def += 1;
            this.maxHp += 10;
            this.hp = this.maxHp;
            levelsGained++;
        }
        return levelsGained;
    }

    public void activate() { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
}
