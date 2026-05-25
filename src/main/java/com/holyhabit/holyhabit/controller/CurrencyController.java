package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.CurrencyResponse;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    // 재화 조회
    @GetMapping
    public ResponseEntity<CurrencyResponse> getCurrency(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(currencyService.getCurrency(userDetails.getUserId()));
    }
}
