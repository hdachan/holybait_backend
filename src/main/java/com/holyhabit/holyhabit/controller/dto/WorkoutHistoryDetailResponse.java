package com.holyhabit.holyhabit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WorkoutHistoryDetailResponse {
    private final String loggedDate;       // "2026-07-05"
    private final int totalSets;           // 총 세트 수
    private final List<SetDetail> sets;

    @Getter
    @AllArgsConstructor
    public static class SetDetail {
        private final int setNumber;
        private final Double weightKg;
        private final Integer reps;
        private final boolean isDropset;

        // isDropset → Jackson 이 "dropset" 으로 직렬화하는 걸 방지
        @JsonProperty("isDropset")
        public boolean isDropset() { return isDropset; }
    }
}