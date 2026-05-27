package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;           // 생명의 숲

    private String description;    // 설명

    @Column(nullable = false)
    private int minLevel;          // 입장 가능 최소 레벨

    @Column(nullable = false)
    private int maxLevel;          // 권장 최대 레벨

    @Column(nullable = false)
    private int shoeCoinCost;      // 입장 신발코인 비용

    private String imageKey;       // Flutter 이미지 키
}
