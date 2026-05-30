package com.holyhabit.holyhabit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "characters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GameCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String imageKey;

    @Column(nullable = false)
    private int baseAtk;

    @Column(nullable = false)
    private int baseDef;

    @Column(nullable = false)
    private int baseHp;

    @Column(nullable = false)
    private double doubleAttackChance;
}
