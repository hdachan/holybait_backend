package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
public class WorkoutRequest {
    private Long routineExerciseId;
    private List<SetRequest> sets;

    @Getter
    public static class SetRequest {
        private BigDecimal weightKg;
        private Integer reps;

        @JsonProperty("isDropset")
        private boolean isDropset;
    }
}