package com.holyhabit.holyhabit.service;  // ← 이거 하나만 바뀜

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
    private final UserRepository userRepository;

    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSetRepository workoutSetRepository;

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

        Routine routine = Routine.builder()
                .user(user)
                .name(name)
                .build();
        routineRepository.save(routine);

        for (int i = 0; i < exerciseIds.size(); i++) {
            Exercise exercise = exerciseRepository.findById(exerciseIds.get(i))
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));

            RoutineExercise re = RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(i)
                    .build();
            routineExerciseRepository.save(re);
        }

        return routine;
    }

    @Transactional
    public Routine updateRoutine(Long routineId, Long userId,
                                 String name, List<Long> exerciseIds) {
        Routine routine = getRoutine(routineId, userId);
        routine.updateName(name);

        routineExerciseRepository.deleteAllByRoutineId(routineId);

        for (int i = 0; i < exerciseIds.size(); i++) {
            Exercise exercise = exerciseRepository.findById(exerciseIds.get(i))
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));

            RoutineExercise re = RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(i)
                    .build();
            routineExerciseRepository.save(re);
        }

        return routine;
    }

    @Transactional
    public Routine saveRoutineDetail(Long routineId, Long userId,
                                     List<RoutineRequest.ExerciseItem> items) {

        Routine routine = getRoutine(routineId, userId);

        // workout_logs → workout_sets 먼저 삭제 후 routine_exercises 삭제
        List<RoutineExercise> existingExercises =
                routineExerciseRepository.findAllByRoutineIdOrderByOrderIndex(routineId);

        for (RoutineExercise re : existingExercises) {
            List<WorkoutLog> logs = workoutLogRepository.findAllByRoutineExerciseId(re.getId());
            for (WorkoutLog log : logs) {
                workoutSetRepository.deleteAllByWorkoutLogId(log.getId());
            }
            workoutLogRepository.deleteAllByRoutineExerciseId(re.getId());
        }

        routineExerciseRepository.deleteAllByRoutineId(routineId);

        for (RoutineRequest.ExerciseItem item : items) {
            Exercise exercise = exerciseRepository.findById(item.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));

            RoutineExercise re = RoutineExercise.builder()
                    .routine(routine)
                    .exercise(exercise)
                    .orderIndex(item.getOrderIndex())
                    .supersetGroup(item.getSupersetGroup())
                    .build();
            routineExerciseRepository.save(re);
        }

        return routine;
    }

    @Transactional
    public void deleteRoutine(Long routineId, Long userId) {
        getRoutine(routineId, userId);
        routineExerciseRepository.deleteAllByRoutineId(routineId);
        routineRepository.deleteById(routineId);
    }
}