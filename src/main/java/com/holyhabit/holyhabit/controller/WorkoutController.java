package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.WorkoutRequest;
import com.holyhabit.holyhabit.controller.dto.WorkoutResponse;
import com.holyhabit.holyhabit.entity.WorkoutLog;
import com.holyhabit.holyhabit.entity.WorkoutSet;
import com.holyhabit.holyhabit.repository.WorkoutSetRepository;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final WorkoutSetRepository workoutSetRepository;

    // 운동 기록 저장
    @PostMapping
    public ResponseEntity<WorkoutResponse> saveWorkout(
            @RequestBody WorkoutRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkoutService.SetRequest> sets = request.getSets().stream()
                .map(s -> new WorkoutService.SetRequest(s.getWeightKg(), s.getReps(), s.isDropset()))
                .toList();

        WorkoutLog log = workoutService.saveWorkoutLog(
                userDetails.getUserId(), request.getRoutineExerciseId(), sets);

        List<WorkoutSet> savedSets = workoutSetRepository
                .findAllByWorkoutLogIdOrderBySetNumber(log.getId());

        return ResponseEntity.ok(new WorkoutResponse(log, savedSets));
    }

    // 최근 운동 기록 조회
    @GetMapping("/recent/{routineExerciseId}")
    public ResponseEntity<List<WorkoutResponse.SetResponse>> getRecentSets(
            @PathVariable Long routineExerciseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WorkoutSet> sets = workoutService.getRecentSets(
                userDetails.getUserId(), routineExerciseId);
        return ResponseEntity.ok(sets.stream().map(WorkoutResponse.SetResponse::new).toList());
    }
}
