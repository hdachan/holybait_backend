package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.RoutineRequest;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public Routine createRoutine(Long userId, String name, List<Long> exerciseIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Routine routine = Routine.builder()
                .user(user)
                .name(name)
                .build();
        routineRepository.save(routine);

        for (int i = 0; i < exerciseIds.size(); i++) {
            Exercise exercise = exerciseRepository.findById(exerciseIds.get(i))
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
            routineExerciseRepository.save(RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(i)
                    .build());
        }
        return routine;
    }

    @Transactional
    public Routine updateRoutine(Long routineId, Long userId, String name, List<Long> exerciseIds) {
        Routine routine = getRoutine(routineId, userId);
        routine.updateName(name);

        // 기존 exercises 삭제 전 FK 순서 지키기
        deleteRoutineExercisesWithDependencies(routineId);

        for (int i = 0; i < exerciseIds.size(); i++) {
            Exercise exercise = exerciseRepository.findById(exerciseIds.get(i))
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
            routineExerciseRepository.save(RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(i)
                    .build());
        }
        return routine;
    }

    @Transactional
    public Routine saveRoutineDetail(Long routineId, Long userId,
            List<RoutineRequest.ExerciseItem> items) {
        Routine routine = getRoutine(routineId, userId);

        // FK 삭제 순서: workout_sets → workout_logs → routine_exercises
        deleteRoutineExercisesWithDependencies(routineId);

        for (RoutineRequest.ExerciseItem item : items) {
            Exercise exercise = exerciseRepository.findById(item.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
            routineExerciseRepository.save(RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(item.getOrderIndex())
                    .supersetGroup(item.getSupersetGroup())
                    .build());
        }
        return routine;
    }

    // routine_exercises 삭제 시 FK 제약 해결
    // 순서: workout_sets → workout_logs → routine_exercises
    private void deleteRoutineExercisesWithDependencies(Long routineId) {
        List<RoutineExercise> existingExercises =
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId);

        for (RoutineExercise re : existingExercises) {
            List<WorkoutLog> logs =
                    workoutLogRepository.findAllByRoutineExerciseId(re.getId());
            for (WorkoutLog log : logs) {
                workoutSetRepository.deleteAllByWorkoutLogId(log.getId());
            }
            workoutLogRepository.deleteAllByRoutineExerciseId(re.getId());
        }
        routineExerciseRepository.deleteAllByRoutineId(routineId);
    }

    @Transactional
    public void deleteRoutine(Long routineId, Long userId) {
        Routine routine = getRoutine(routineId, userId);
        deleteRoutineExercisesWithDependencies(routineId);
        routineRepository.delete(routine);
    }

    @Transactional(readOnly = true)
    public List<RoutineExercise> getRoutineExercises(Long routineId, Long userId) {
        getRoutine(routineId, userId); // 본인 루틴인지 확인
        return routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId);
    }
}
