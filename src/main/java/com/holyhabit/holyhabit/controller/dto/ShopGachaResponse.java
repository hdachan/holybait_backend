package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShopGachaResponse {
    private final String characterName;
    private final String characterImageKey;
    private final int remainingGold;    // 뽑기 후 남은 골드
    private final int newStatId;        // 새로 생성된 CharacterStat ID
}