package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    // isCustom → Jackson 이 "custom" 으로 직렬화하는 걸 방지 (앱은 isCustom 으로 읽음)
    @JsonProperty("isCustom")
    public boolean isCustom() { return isCustom; }
}
