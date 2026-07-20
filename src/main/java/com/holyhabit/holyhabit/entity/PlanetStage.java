package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "planet_stages",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"planet_id", "stage_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class PlanetStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    private Planet planet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 1; // 탐험 구역 순서 (1, 2, 3...)
}