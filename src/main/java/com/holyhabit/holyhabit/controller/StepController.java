package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.StepRewardResponse;
import com.holyhabit.holyhabit.controller.dto.StepSaveRequest;
import com.holyhabit.holyhabit.controller.dto.StepRewardRequest;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.StepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/steps")
@RequiredArgsConstructor
public class StepController {

    private final StepService stepService;

    // 걸음 수 보상 받기
    // POST /steps/reward { totalSteps: 2500 }
    @PostMapping("/reward")
    public ResponseEntity<StepRewardResponse> claimReward(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody StepRewardRequest request
    ) {
        StepRewardResponse response = stepService.claimStepReward(
                userDetails.getUserId(),
                request.getTotalSteps()
        );
        return ResponseEntity.ok(response);
    }

    // 걸음 수 저장 (자정 — 다음날 앱 켤 때 어제 날짜 걸음 수 전송)
    // POST /steps/save { stepCount: 3100, date: "2026-07-01" }
    @PostMapping("/save")
    public ResponseEntity<Void> saveStepLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody StepSaveRequest request
    ) {
        stepService.saveStepLog(
                userDetails.getUserId(),
                request.getStepCount(),
                request.getDate()
        );
        return ResponseEntity.ok().build();
    }
}