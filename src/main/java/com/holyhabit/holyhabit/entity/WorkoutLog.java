package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_logs",
        indexes = {
                @Index(
                        name = "idx_workout_logs_user_exercise_date",
                        columnList = "user_id, routine_exercise_id, logged_at"
                ),
                @Index(
                        name = "idx_workout_logs_user_ex_date",
                        columnList = "user_id, exercise_id, logged_at"
                )
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 기록은 운동 기준으로 저장 (루틴과 무관하게 유지)
    // DB 컬럼은 기존 데이터 이전을 위해 NULL 허용 — 코드에서는 항상 채움
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    // 마지막으로 기록한 루틴 안의 운동 (참고용)
    // 루틴에서 운동이 빠지거나 루틴이 삭제되면 null 로 끊고 기록은 남김
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id")
    private RoutineExercise routineExercise;

    @Column(nullable = false)
    private LocalDateTime loggedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.loggedAt == null) this.loggedAt = LocalDateTime.now();
    }

    // 같은 날 다른 루틴에서 같은 운동을 다시 저장한 경우 참조 루틴 갱신
    public void updateRoutineExercise(RoutineExercise routineExercise) {
        this.routineExercise = routineExercise;
    }
}