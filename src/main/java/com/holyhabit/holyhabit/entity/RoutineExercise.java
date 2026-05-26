package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "routine_exercises")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class RoutineExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private int orderIndex;

    // null = 슈퍼세트 아님, 같은 숫자 = 같은 슈퍼세트 그룹
    private Integer supersetGroup;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 슈퍼세트 해제/순서 변경 시 workout 데이터 유지하면서 메타 정보만 업데이트
    public void updateOrderAndSuperset(int orderIndex, Integer supersetGroup) {
        this.orderIndex = orderIndex;
        this.supersetGroup = supersetGroup;
    }
}
