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
    private int expReward;         // 처치 시 경험치

    @Column(nullable = false)
    private int goldReward;        // 처치 시 골드

    // 더블어택 확률 (0.0 ~ 1.0)
    @Column(nullable = false)
    private double doubleAttackChance;

    private String imageKey;       // Flutter 이미지 키
}
