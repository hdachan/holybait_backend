package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.ShopGachaResponse;
import com.holyhabit.holyhabit.controller.dto.ShopPricesResponse;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.AdventureService;
import com.holyhabit.holyhabit.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // 가격 정보 — GET /shop/prices
    @GetMapping("/prices")
    public ResponseEntity<ShopPricesResponse> prices() {
        return ResponseEntity.ok(new ShopPricesResponse(
                ShopService.GACHA_COST, AdventureService.getSlotCosts()));
    }

    // 캐릭터 뽑기 — POST /shop/gacha
    @PostMapping("/gacha")
    public ResponseEntity<ShopGachaResponse> gacha(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(shopService.gacha(userDetails.getUserId()));
    }
}