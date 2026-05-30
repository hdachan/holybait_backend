package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.AdventureConfirmResponse;
import com.holyhabit.holyhabit.controller.dto.AdventureStartResponse;
import com.holyhabit.holyhabit.controller.dto.CharacterStatResponse;
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

    // 스테이지 목록
    @GetMapping("/stages")
    public ResponseEntity<List<Stage>> getStages(
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(adventureService.getStages());
    }

    // 내 모든 캐릭터 목록
    @GetMapping("/characters")
    public ResponseEntity<List<CharacterStatResponse>> getMyCharacters(
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(
                adventureService.getAllStats(u.getUserId()).stream()
                        .map(CharacterStatResponse::new)
                        .toList());
    }

    // 현재 활성 캐릭터
    @GetMapping("/character")
    public ResponseEntity<CharacterStatResponse> getActiveCharacter(
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(
                new CharacterStatResponse(adventureService.getActiveStat(u.getUserId())));
    }

    // 캐릭터 변경 (statId = character_stats.id)
    @PostMapping("/character/select")
    public ResponseEntity<CharacterStatResponse> selectCharacter(
            @RequestParam Long statId,
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(
                new CharacterStatResponse(
                        adventureService.selectCharacter(u.getUserId(), statId)));
    }

    // 모험 시작
    @PostMapping("/start")
    public ResponseEntity<AdventureStartResponse> startBattle(
            @RequestParam Long stageId,
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(adventureService.startBattle(u.getUserId(), stageId));
    }

    // 보상 확인
    @PostMapping("/{battleId}/confirm")
    public ResponseEntity<AdventureConfirmResponse> confirmRewards(
            @PathVariable Long battleId,
            @AuthenticationPrincipal CustomUserDetails u) {
        return ResponseEntity.ok(adventureService.confirmRewards(u.getUserId(), battleId));
    }
}
