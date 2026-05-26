package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class WorkoutRequest {

    private Long routineExerciseId;
    private List<SetRequest> sets;

    public Long getRoutineExerciseId() { return routineExerciseId; }
    public List<SetRequest> getSets() { return sets; }

    public static class SetRequest {
        private BigDecimal weightKg;
        private Integer reps;

        @JsonProperty("isDropset")
        private boolean isDropset;

        public BigDecimal getWeightKg() { return weightKg; }
        public Integer getReps() { return reps; }
        public boolean isDropset() { return isDropset; }
    }
}
