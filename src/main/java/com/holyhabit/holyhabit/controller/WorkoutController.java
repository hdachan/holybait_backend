package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.RecentSetsResponse;
import com.holyhabit.holyhabit.controller.dto.WorkoutRequest;
import com.holyhabit.holyhabit.controller.dto.WorkoutResponse;
import com.holyhabit.holyhabit.entity.WorkoutLog;
import com.holyhabit.holyhabit.entity.WorkoutSet;
import com.holyhabit.holyhabit.repository.WorkoutLogRepository;
import com.holyhabit.holyhabit.repository.WorkoutSetRepository;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final WorkoutSetRepository workoutSetRepository;
    private final WorkoutLogRepository workoutLogRepository;

    // 운동 기록 저장
    // 슈퍼세트/단일 구분 없이 항상 코인 지급 (각 운동 독립 계산)
    @PostMapping
    public ResponseEntity<WorkoutResponse> saveWorkout(
            @RequestBody WorkoutRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkoutService.SetRequest> sets = request.getSets().stream()
                .map(s -> new WorkoutService.SetRequest(
                        s.getWeightKg(), s.getReps(), s.isDropset()))
                .toList();

        WorkoutService.SaveResult result = workoutService.saveWorkoutLog(
                userDetails.getUserId(),
                request.getRoutineExerciseId(),
                sets,
                true // 항상 코인 지급 — 내부에서 오늘 받은 양 비교해서 차이만 지급
        );

        List<WorkoutSet> savedSets = workoutSetRepository
                .findAllByWorkoutLogIdOrderBySetNumber(result.log().getId());

        return ResponseEntity.ok(
                new WorkoutResponse(result.log(), savedSets, result.grantedShoeCoin()));
    }

    // 최근 운동 기록 조회
    // loggedAt 을 최상위에 포함해서 Flutter 에서 오늘/이전 구분 명확히
    @GetMapping("/recent/{routineExerciseId}")
    public ResponseEntity<RecentSetsResponse> getRecentSets(
            @PathVariable Long routineExerciseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Optional<WorkoutLog> logOpt = workoutLogRepository
                .findTopByUserIdAndRoutineExerciseIdOrderByLoggedAtDesc(
                        userDetails.getUserId(), routineExerciseId);

        if (logOpt.isEmpty()) return ResponseEntity.ok(null);

        WorkoutLog log = logOpt.get();
        List<WorkoutSet> sets = workoutSetRepository
                .findAllByWorkoutLogIdOrderBySetNumber(log.getId());

        return ResponseEntity.ok(new RecentSetsResponse(log, sets));
    }
}
