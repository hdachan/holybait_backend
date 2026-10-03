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

    // 오늘 이전의 가장 최근 기록 — 오늘 기록이 있어도 "최근 기록"에 지난번 기록을 보여주기 위함
    private final String previousLoggedDate;   // 없으면 null
    private final List<SetInfo> previousSets;  // 없으면 빈 목록

    public RecentSetsResponse(WorkoutLog log, List<WorkoutSet> sets,
                              WorkoutLog previousLog, List<WorkoutSet> previousSets) {
        // LocalDateTime → "yyyy-MM-dd" 문자열 (앞 10자리)
        this.loggedDate = log.getLoggedAt().toLocalDate().toString();
        this.sets = sets.stream().map(SetInfo::new).toList();
        this.previousLoggedDate = previousLog != null
                ? previousLog.getLoggedAt().toLocalDate().toString() : null;
        this.previousSets = previousSets.stream().map(SetInfo::new).toList();
    }

    public String getLoggedDate() { return loggedDate; }
    public List<SetInfo> getSets() { return sets; }
    public String getPreviousLoggedDate() { return previousLoggedDate; }
    public List<SetInfo> getPreviousSets() { return previousSets; }

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
