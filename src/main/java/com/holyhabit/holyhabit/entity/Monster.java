package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monsters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int hp;

    @Column(nullable = false)
    private int atk;

    @Column(nullable = false)
    private int def;

    @Column(nullable = false)
    private int expReward;

    @Column(nullable = false)
    private int goldReward;

    @Column(nullable = false)
    private double doubleAttackChance;

    private String imageKey;

    // 처치 시 드롭되는 캐릭터 (null이면 드롭 없음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drop_character_id", nullable = true)
    private GameCharacter dropCharacter;

    // 캐릭터 드롭 확률 (0.0 ~ 1.0, 기본값 0%)
    @Column(nullable = false)
    @Builder.Default
    private double dropChance = 0.0;
}