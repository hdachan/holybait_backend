package com.holyhabit.holyhabit.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class StepSaveRequest {
    private int stepCount;   // 해당 날의 최종 걸음 수
    private LocalDate date;  // 저장할 날짜 (어제 날짜)
}