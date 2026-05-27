package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.AdventureConfirmResponse;
import com.holyhabit.holyhabit.controller.dto.AdventureStartResponse;
import com.holyhabit.holyhabit.controller.dto.CharacterStatResponse;
import com.holyhabit.holyhabit.entity.CharacterStat;
import com.holyhabit.holyhabit.entity.Stage;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.AdventureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adventures")
@RequiredArgsConstructor
public class AdventureController {

    private final AdventureService adventureService;

    // 스테이지 목록 조회
    @GetMapping("/stages")
    public ResponseEntity<List<Stage>> getStages(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(adventureService.getStages());
    }

    // 캐릭터 스탯 조회 — DTO 반환 (Lazy 프록시 직렬화 오류 방지)
    @GetMapping("/character")
    public ResponseEntity<CharacterStatResponse> getCharacterStat(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CharacterStat stat = adventureService.getOrCreateStat(userDetails.getUserId());
        return ResponseEntity.ok(new CharacterStatResponse(stat));
    }

    // 모험 시작 — 신발코인 소모 + 배틀 계산
    @PostMapping("/start")
    public ResponseEntity<AdventureStartResponse> startBattle(
            @RequestParam Long stageId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                adventureService.startBattle(userDetails.getUserId(), stageId));
    }

    // 보상 확인 — 마지막 버튼 눌러야 보상 지급
    @PostMapping("/{battleId}/confirm")
    public ResponseEntity<AdventureConfirmResponse> confirmRewards(
            @PathVariable Long battleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                adventureService.confirmRewards(userDetails.getUserId(), battleId));
    }
}
