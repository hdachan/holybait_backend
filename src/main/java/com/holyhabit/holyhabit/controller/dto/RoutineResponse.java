package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.Routine;
import com.holyhabit.holyhabit.entity.RoutineExercise;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class RoutineResponse {
    private final Long id;
    private final String name;
    private final int exerciseCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private List<RoutineExerciseResponse> exercises;

    public RoutineResponse(Routine routine) {
        this.id = routine.getId();
        this.name = routine.getName();
        this.exerciseCount = 0;
        this.createdAt = routine.getCreatedAt();
        this.updatedAt = routine.getUpdatedAt();
    }

    public RoutineResponse(Routine routine, List<RoutineExercise> exercises) {
        this.id = routine.getId();
        this.name = routine.getName();
        this.exerciseCount = exercises.size();
        this.createdAt = routine.getCreatedAt();
        this.updatedAt = routine.getUpdatedAt();
        this.exercises = exercises.stream().map(RoutineExerciseResponse::new).toList();
    }

    @Getter
    public static class RoutineExerciseResponse {
        private final Long id;
        private final Long exerciseId;
        private final String exerciseName;
        private final String target;
        private final int orderIndex;
        private final Integer supersetGroup;

        public RoutineExerciseResponse(RoutineExercise re) {
            this.id = re.getId();
            this.exerciseId = re.getExercise().getId();
            this.exerciseName = re.getExercise().getName();
            this.target = re.getExercise().getTarget();
            this.orderIndex = re.getOrderIndex();
            this.supersetGroup = re.getSupersetGroup();
        }
    }
}
