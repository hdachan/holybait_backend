package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.RoutineRequest;
import com.holyhabit.holyhabit.controller.dto.RoutineResponse;
import com.holyhabit.holyhabit.entity.Routine;
import com.holyhabit.holyhabit.entity.RoutineExercise;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routines")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;

    // 루틴 목록 조회
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> getRoutines(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Routine> routines = routineService.getRoutines(userDetails.getUserId());
        List<RoutineResponse> response = routines.stream()
                .map(r -> {
                    List<RoutineExercise> exercises = routineService.getRoutineExercises(
                            r.getId(), userDetails.getUserId());
                    return new RoutineResponse(r, exercises);
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    // 루틴 단건 조회
    @GetMapping("/{routineId}")
    public ResponseEntity<RoutineResponse> getRoutine(
            @PathVariable Long routineId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Routine routine = routineService.getRoutine(routineId, userDetails.getUserId());
        List<RoutineExercise> exercises = routineService.getRoutineExercises(
                routineId, userDetails.getUserId());
        return ResponseEntity.ok(new RoutineResponse(routine, exercises));
    }

    // 루틴 추가
    @PostMapping
    public ResponseEntity<RoutineResponse> createRoutine(
            @RequestBody RoutineRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Routine routine = routineService.createRoutine(
                userDetails.getUserId(), request.getName(), request.getExerciseIds());
        List<RoutineExercise> exercises = routineService.getRoutineExercises(
                routine.getId(), userDetails.getUserId());
        return ResponseEntity.ok(new RoutineResponse(routine, exercises));
    }

    // 루틴 수정 (form 화면용)
    @PutMapping("/{routineId}")
    public ResponseEntity<RoutineResponse> updateRoutine(
            @PathVariable Long routineId,
            @RequestBody RoutineRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Routine routine = routineService.updateRoutine(
                routineId, userDetails.getUserId(),
                request.getName(), request.getExerciseIds());
        List<RoutineExercise> exercises = routineService.getRoutineExercises(
                routineId, userDetails.getUserId());
        return ResponseEntity.ok(new RoutineResponse(routine, exercises));
    }

    // 편집 모드 전체 저장 (순서 + 슈퍼세트 + 새 운동 한번에)
    @PatchMapping("/{routineId}/detail")
    public ResponseEntity<RoutineResponse> saveDetail(
            @PathVariable Long routineId,
            @RequestBody RoutineRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Routine routine = routineService.saveRoutineDetail(
                routineId, userDetails.getUserId(), request.getExercises());
        List<RoutineExercise> exercises = routineService.getRoutineExercises(
                routineId, userDetails.getUserId());
        return ResponseEntity.ok(new RoutineResponse(routine, exercises));
    }

    // 루틴 삭제
    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> deleteRoutine(
            @PathVariable Long routineId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        routineService.deleteRoutine(routineId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}
