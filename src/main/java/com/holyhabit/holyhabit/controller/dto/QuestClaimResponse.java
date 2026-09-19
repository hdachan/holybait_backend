package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestClaimResponse {
    private int rewardGold;
    private int remainingGold;
}