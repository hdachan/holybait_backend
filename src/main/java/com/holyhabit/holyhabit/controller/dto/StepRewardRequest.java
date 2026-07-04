package com.holyhabit.holyhabit.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StepRewardRequest {
    private int totalSteps; // 오늘 총 걸음 수 (폰에서 전송)
}