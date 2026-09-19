package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.QuestClaimResponse;
import com.holyhabit.holyhabit.controller.dto.QuestResponse;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.CurrencyService;
import com.holyhabit.holyhabit.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final CurrencyService currencyService;

    // 내 퀘스트 목록 조회
    // GET /quests?type=tutorial
    @GetMapping
    public ResponseEntity<List<QuestResponse>> getMyQuests(
            @RequestParam(defaultValue = "tutorial") String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<QuestResponse> result =
                questService.getMyQuests(userDetails.getUserId(), type);
        return ResponseEntity.ok(result);
    }

    // 보상 수령
    // POST /quests/{questId}/claim
    @PostMapping("/{questId}/claim")
    public ResponseEntity<QuestClaimResponse> claimReward(
            @PathVariable Long questId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int rewardGold = questService.claimReward(userDetails.getUserId(), questId);
        int remainingGold = currencyService.getGold(userDetails.getUserId());
        return ResponseEntity.ok(new QuestClaimResponse(rewardGold, remainingGold));
    }
}