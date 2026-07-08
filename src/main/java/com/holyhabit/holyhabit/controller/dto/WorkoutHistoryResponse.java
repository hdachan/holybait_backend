package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkoutHistoryResponse {
    private final Long exerciseId;
    private final String exerciseName;
    private final String target;
    private final String lastLoggedDate; // "2026-07-05"
    private final int totalSessions;    // 총 운동 횟수
}