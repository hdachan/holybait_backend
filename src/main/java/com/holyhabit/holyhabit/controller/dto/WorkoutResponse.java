package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.holyhabit.holyhabit.entity.WorkoutLog;
import com.holyhabit.holyhabit.entity.WorkoutSet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WorkoutResponse {

    private final Long id;
    private final Long routineExerciseId;
    private final LocalDateTime loggedAt;
    private final List<SetResponse> sets;
    private final int grantedShoeCoin;

    public WorkoutResponse(WorkoutLog log, List<WorkoutSet> sets, int grantedShoeCoin) {
        this.id = log.getId();
        this.routineExerciseId = log.getRoutineExercise().getId();
        this.loggedAt = log.getLoggedAt();
        this.sets = sets.stream()
                .map(ws -> new SetResponse(ws, log.getLoggedAt()))
                .toList();
        this.grantedShoeCoin = grantedShoeCoin;
    }

    public Long getId() { return id; }
    public Long getRoutineExerciseId() { return routineExerciseId; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public List<SetResponse> getSets() { return sets; }
    public int getGrantedShoeCoin() { return grantedShoeCoin; }

    public static class SetResponse {

        private final int setNumber;
        private final BigDecimal weightKg;
        private final Integer reps;
        private final boolean isDropset;
        private final LocalDateTime loggedAt; // 오늘/이전 구분용

        public SetResponse(WorkoutSet ws, LocalDateTime loggedAt) {
            this.setNumber = ws.getSetNumber();
            this.weightKg = ws.getWeightKg();
            this.reps = ws.getReps();
            this.isDropset = ws.isDropset();
            this.loggedAt = loggedAt; // WorkoutLog.loggedAt 직접 전달
        }

        public int getSetNumber() { return setNumber; }
        public BigDecimal getWeightKg() { return weightKg; }
        public Integer getReps() { return reps; }

        // isDropset → Jackson이 "dropset" 으로 직렬화하는 걸 방지
        @JsonProperty("isDropset")
        public boolean isDropset() { return isDropset; }

        // loggedAt 반드시 직렬화
        public LocalDateTime getLoggedAt() { return loggedAt; }
    }
}
