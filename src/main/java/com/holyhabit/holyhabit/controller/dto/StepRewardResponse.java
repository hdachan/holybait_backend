package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StepRewardResponse {
    private int grantedCoins;     // 이번에 받은 코인
    private int todayTotalEarned; // 오늘 총 받은 코인
    private int dailyCap;         // 하루 최대치
    private boolean success;      // 지급 성공 여부
}