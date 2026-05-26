package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.RoutineRequest;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final WorkoutSetRepository workoutSetRepository;
    private final UserRepository userRepository;

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
        return routine;
    }

    @Transactional
    public Routine updateRoutine(Long routineId, Long userId,
            String name, List<Long> exerciseIds) {
        Routine routine = getRoutine(routineId, userId);
        routine.updateName(name);
        deleteRoutineExercisesWithDependencies(routineId);

        for (int i = 0; i < exerciseIds.size(); i++) {
            Exercise exercise = exerciseRepository.findById(exerciseIds.get(i))
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
            routineExerciseRepository.save(RoutineExercise.builder()
                    .routine(routine).exercise(exercise).orderIndex(i).build());
        }
        return routine;
    }

    @Transactional
    public Routine saveRoutineDetail(Long routineId, Long userId,
            List<RoutineRequest.ExerciseItem> items) {
        Routine routine = getRoutine(routineId, userId);

        List<RoutineExercise> existing =
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId);

        // 새 목록의 exerciseId 세트
        Set<Long> newExerciseIds = items.stream()
                .map(RoutineRequest.ExerciseItem::getExerciseId)
                .collect(Collectors.toSet());

        // 제거된 운동 → workout 데이터 포함해서 삭제
        for (RoutineExercise re : existing) {
            if (!newExerciseIds.contains(re.getExercise().getId())) {
                List<WorkoutLog> logs =
                        workoutLogRepository.findAllByRoutineExerciseId(re.getId());
                for (WorkoutLog log : logs) {
                    workoutSetRepository.deleteAllByWorkoutLogId(log.getId());
                }
                workoutLogRepository.deleteAllByRoutineExerciseId(re.getId());
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

        // 새 목록 처리
        for (RoutineRequest.ExerciseItem item : items) {
            if (existingMap.containsKey(item.getExerciseId())) {
                // 기존 운동 → orderIndex, supersetGroup 만 업데이트 (workout 데이터 보존)
                existingMap.get(item.getExerciseId())
                        .updateOrderAndSuperset(item.getOrderIndex(), item.getSupersetGroup());
            } else {
                // 새 운동 추가
                Exercise exercise = exerciseRepository.findById(item.getExerciseId())
                        .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
                routineExerciseRepository.save(RoutineExercise.builder()
                        .routine(routine)
                        .exercise(exercise)
                        .orderIndex(item.getOrderIndex())
                        .supersetGroup(item.getSupersetGroup())
                        .build());
            }
        }

        return routine;
    }

    @Transactional
    public void deleteRoutine(Long routineId, Long userId) {
        getRoutine(routineId, userId);
        deleteRoutineExercisesWithDependencies(routineId);
        routineRepository.deleteById(routineId);
    }

    // 루틴 삭제 시 전체 삭제 (FK 순서 지키기)
    private void deleteRoutineExercisesWithDependencies(Long routineId) {
        List<RoutineExercise> exercises =
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId);
        for (RoutineExercise re : exercises) {
            List<WorkoutLog> logs =
                    workoutLogRepository.findAllByRoutineExerciseId(re.getId());
            for (WorkoutLog log : logs) {
                workoutSetRepository.deleteAllByWorkoutLogId(log.getId());
            }
            workoutLogRepository.deleteAllByRoutineExerciseId(re.getId());
        }
        routineExerciseRepository.deleteAllByRoutineId(routineId);
    }
}
