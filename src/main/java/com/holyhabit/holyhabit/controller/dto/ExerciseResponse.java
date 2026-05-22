package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.Exercise;
import lombok.Getter;

@Getter
public class ExerciseResponse {
    private final Long id;
    private final String name;
    private final String target;
    private final boolean isCustom;

    public ExerciseResponse(Exercise exercise) {
        this.id = exercise.getId();
        this.name = exercise.getName();
        this.target = exercise.getTarget();
        this.isCustom = exercise.isCustom();
    }
}
