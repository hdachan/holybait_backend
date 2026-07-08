package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.RecentSetsResponse;
import com.holyhabit.holyhabit.controller.dto.WorkoutHistoryDetailResponse;
import com.holyhabit.holyhabit.controller.dto.WorkoutHistoryResponse;
import com.holyhabit.holyhabit.controller.dto.WorkoutSummaryResponse;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final WorkoutSetRepository workoutSetRepository;
    private final WorkoutLogRepository workoutLogRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 운동 기록 저장
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
                true
        );

        List<WorkoutSet> savedSets = workoutSetRepository
                .findAllByWorkoutLogIdOrderBySetNumber(result.log().getId());

        return ResponseEntity.ok(
                new WorkoutResponse(result.log(), savedSets, result.grantedShoeCoin()));
    }

    // 최근 운동 기록 조회
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

    // 운동 통계
    // GET /workouts/summary
    @GetMapping("/summary")
    public ResponseEntity<WorkoutSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                workoutService.getSummary(userDetails.getUserId()));
    }

    // 내가 운동한 종목 목록 (전체 기록용)
    // GET /workouts/exercises
    @GetMapping("/exercises")
    public ResponseEntity<List<WorkoutHistoryResponse>> getExerciseHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkoutHistoryResponse> result =
                workoutService.getExerciseHistory(userDetails.getUserId());
        return ResponseEntity.ok(result);
    }

    // 특정 종목 날짜별 기록
    // GET /workouts/exercises/{exerciseId}/history
    @GetMapping("/exercises/{exerciseId}/history")
    public ResponseEntity<List<WorkoutHistoryDetailResponse>> getExerciseDetail(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkoutHistoryDetailResponse> result =
                workoutService.getExerciseDetail(
                        userDetails.getUserId(), exerciseId);
        return ResponseEntity.ok(result);
    }
}