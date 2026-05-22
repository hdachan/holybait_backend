package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.WorkoutLog;
import com.holyhabit.holyhabit.entity.WorkoutSet;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class WorkoutResponse {
    private final Long id;
    private final Long routineExerciseId;
    private final LocalDateTime loggedAt;
    private final List<SetResponse> sets;

    public WorkoutResponse(WorkoutLog log, List<WorkoutSet> sets) {
        this.id = log.getId();
        this.routineExerciseId = log.getRoutineExercise().getId();
        this.loggedAt = log.getLoggedAt();
        this.sets = sets.stream().map(SetResponse::new).toList();
    }

    @Getter
    public static class SetResponse {
        private final int setNumber;
        private final BigDecimal weightKg;
        private final Integer reps;
        private final boolean isDropset;

        public SetResponse(WorkoutSet ws) {
            this.setNumber = ws.getSetNumber();
            this.weightKg = ws.getWeightKg();
            this.reps = ws.getReps();
            this.isDropset = ws.isDropset();
        }
    }
}
