package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrencyResponse {
    private final int gold;
    private final int shoeCoin;
    private final int todayShoeCoin; // 오늘 받은 신발코인
    private final int dailyCap;      // 하루 최대치
}
