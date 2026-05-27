package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "battle_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BattleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @Column(nullable = false)
    private int turn;

    // PLAYER or MONSTER
    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private int damage;

    @Column(nullable = false)
    private boolean isDoubleAttack;

    // 이 턴 후 플레이어/몬스터 HP
    @Column(nullable = false)
    private int playerHpAfter;

    @Column(nullable = false)
    private int monsterHpAfter;
}
