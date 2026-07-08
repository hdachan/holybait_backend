package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkoutSummaryResponse {
    private final int thisWeekCount;    // 이번 주 운동 횟수
    private final int streakDays;       // 연속 운동 일수
    private final int totalExercises;   // 총 운동 종목 수
}