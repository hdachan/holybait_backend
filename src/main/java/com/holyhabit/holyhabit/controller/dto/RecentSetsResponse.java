package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.holyhabit.holyhabit.entity.WorkoutLog;
import com.holyhabit.holyhabit.entity.WorkoutSet;

import java.math.BigDecimal;
import java.util.List;

public class RecentSetsResponse {

    // "2026-05-26" 형식 — Flutter 에서 오늘 날짜 문자열과 단순 비교
    private final String loggedDate;
    private final List<SetInfo> sets;

    public RecentSetsResponse(WorkoutLog log, List<WorkoutSet> sets) {
        // LocalDateTime → "yyyy-MM-dd" 문자열 (앞 10자리)
        this.loggedDate = log.getLoggedAt().toLocalDate().toString();
        this.sets = sets.stream().map(SetInfo::new).toList();
    }

    public String getLoggedDate() { return loggedDate; }
    public List<SetInfo> getSets() { return sets; }

    public static class SetInfo {
        private final int setNumber;
        private final BigDecimal weightKg;
        private final Integer reps;
        private final boolean isDropset;

        public SetInfo(WorkoutSet ws) {
            this.setNumber = ws.getSetNumber();
            this.weightKg = ws.getWeightKg();
            this.reps = ws.getReps();
            this.isDropset = ws.isDropset();
        }

        public int getSetNumber() { return setNumber; }
        public BigDecimal getWeightKg() { return weightKg; }
        public Integer getReps() { return reps; }

        @JsonProperty("isDropset")
        public boolean isDropset() { return isDropset; }
    }
}
