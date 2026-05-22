package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.ExerciseRequest;
import com.holyhabit.holyhabit.controller.dto.ExerciseResponse;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    // 전체 운동 목록 조회 (검색 + 카테고리 필터)
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getExercises(
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        List<ExerciseResponse> response;

        if (keyword != null && !keyword.isBlank()) {
            response = exerciseService.searchExercises(userId, keyword)
                    .stream().map(ExerciseResponse::new).toList();
        } else if (target != null && !target.isBlank()) {
            response = exerciseService.getExercisesByTarget(userId, target)
                    .stream().map(ExerciseResponse::new).toList();
        } else {
            response = exerciseService.getExercises(userId)
                    .stream().map(ExerciseResponse::new).toList();
        }

        return ResponseEntity.ok(response);
    }

    // 커스텀 운동 추가
    @PostMapping("/custom")
    public ResponseEntity<ExerciseResponse> createCustomExercise(
            @RequestBody ExerciseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(new ExerciseResponse(
                exerciseService.createCustomExercise(
                        userDetails.getUserId(), request.getName(), request.getTarget())
        ));
    }

    // 커스텀 운동 삭제
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteCustomExercise(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        exerciseService.deleteCustomExercise(exerciseId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}
