package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.RoutineRequest;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;
    private final QuestService questService;

    @Transactional(readOnly = true)
    public List<Routine> getRoutines(Long userId) {
        return routineRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Routine getRoutine(Long routineId, Long userId) {
        return routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RuntimeException("루틴을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<RoutineExercise> getRoutineExercises(Long routineId, Long userId) {
        getRoutine(routineId, userId);
        return routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId);
    }

    @Transactional
    public Routine createRoutine(Long userId, String name, List<Long> exerciseIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Routine routine = Routine.builder().user(user).name(name).build();
        routineRepository.save(routine);

        for (int i = 0; i < exerciseIds.size(); i++) {
            Exercise exercise = exerciseRepository.findById(exerciseIds.get(i))
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
            routineExerciseRepository.save(RoutineExercise.builder()
                    .routine(routine).exercise(exercise).orderIndex(i).build());
        }

        // 퀘스트 진행도 갱신 (첫 루틴 만들기)
        questService.progressQuest(userId, "create_routine");

        return routine;
    }

    // 루틴 수정 (form 화면용) — 바뀐 것만 반영, 운동 기록은 보존
    @Transactional
    public Routine updateRoutine(Long routineId, Long userId,
                                 String name, List<Long> exerciseIds) {
        Routine routine = getRoutine(routineId, userId);
        routine.updateName(name);

        // form 화면은 슈퍼세트 정보를 모르므로 남아 있는 운동은 기존 그룹 유지
        Map<Long, Integer> currentGroups = new HashMap<>();
        for (RoutineExercise re :
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId)) {
            currentGroups.putIfAbsent(re.getExercise().getId(), re.getSupersetGroup());
        }

        List<ExerciseSlot> slots = new ArrayList<>();
        for (int i = 0; i < exerciseIds.size(); i++) {
            Long exerciseId = exerciseIds.get(i);
            slots.add(new ExerciseSlot(exerciseId, i, currentGroups.get(exerciseId)));
        }
        syncExercises(routine, slots);
        return routine;
    }

    // 편집 모드 전체 저장 (순서 + 슈퍼세트 + 새 운동)
    @Transactional
    public Routine saveRoutineDetail(Long routineId, Long userId,
                                     List<RoutineRequest.ExerciseItem> items) {
        Routine routine = getRoutine(routineId, userId);
        syncExercises(routine, items.stream()
                .map(item -> new ExerciseSlot(
                        item.getExerciseId(), item.getOrderIndex(), item.getSupersetGroup()))
                .toList());
        return routine;
    }

    // 루틴 삭제 — 운동 기록은 남기고 루틴 연결만 끊음
    @Transactional
    public void deleteRoutine(Long routineId, Long userId) {
        getRoutine(routineId, userId);
        for (RoutineExercise re :
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId)) {
            workoutLogRepository.detachRoutineExercise(re.getId());
        }
        routineExerciseRepository.deleteAllByRoutineId(routineId);
        routineRepository.deleteById(routineId);
    }

    // 루틴의 운동 목록을 slots 와 같게 맞춤
    // 남는 운동 → 순서·슈퍼세트만 갱신 / 빠진 운동 → 루틴에서만 제거(기록 보존) / 새 운동 → 추가
    private void syncExercises(Routine routine, List<ExerciseSlot> slots) {
        List<RoutineExercise> existing =
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routine.getId());

        Set<Long> newExerciseIds = slots.stream()
                .map(ExerciseSlot::exerciseId)
                .collect(Collectors.toSet());

        for (RoutineExercise re : existing) {
            if (!newExerciseIds.contains(re.getExercise().getId())) {
                workoutLogRepository.detachRoutineExercise(re.getId());
                routineExerciseRepository.delete(re);
            }
        }

        // 기존 운동 map (exerciseId → RoutineExercise)
        Map<Long, RoutineExercise> existingMap = existing.stream()
                .collect(Collectors.toMap(
                        re -> re.getExercise().getId(),
                        re -> re,
                        (a, b) -> a
                ));

        for (ExerciseSlot slot : slots) {
            RoutineExercise current = existingMap.get(slot.exerciseId());
            if (current != null) {
                current.updateOrderAndSuperset(slot.orderIndex(), slot.supersetGroup());
            } else {
                Exercise exercise = exerciseRepository.findById(slot.exerciseId())
                        .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
                routineExerciseRepository.save(RoutineExercise.builder()
                        .routine(routine)
                        .exercise(exercise)
                        .orderIndex(slot.orderIndex())
                        .supersetGroup(slot.supersetGroup())
                        .build());
            }
        }
    }

    private record ExerciseSlot(Long exerciseId, int orderIndex, Integer supersetGroup) {}
}