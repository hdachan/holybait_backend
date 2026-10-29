package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 앱이 화면에 보여줄 가격 — 서버 값만 바꾸면 앱 업데이트 없이 반영됨
@Getter
@AllArgsConstructor
public class ShopPricesResponse {
    private final int gachaCost;           // 뽑기 1회 비용
    private final List<Integer> slotCosts; // 슬롯 확장 가격표 (인덱스 = 현재 슬롯 수)
}
