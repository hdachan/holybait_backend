package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SlotExpandResponse {
    private final int slotCount;     // 확장 후 슬롯 수
    private final int remainingGold; // 남은 골드
    private final int nextSlotCost;  // 다음 슬롯 확장 비용 (최대면 -1)
}