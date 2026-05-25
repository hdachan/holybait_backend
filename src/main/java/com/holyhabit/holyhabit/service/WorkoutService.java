package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService; // 추가

    // 운동 기록 저장
    @Transactional
    public WorkoutLog saveWorkoutLog(Long userId, Long routineExerciseId, List<SetRequest> sets) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        RoutineExercise routineExercise = routineExerciseRepository.findById(routineExerciseId)
                .orElseThrow(() -> new RuntimeException("루틴 운동을 찾을 수 없습니다."));

        WorkoutLog log = WorkoutLog.builder()
                .user(user)
                .routineExercise(routineExercise)
                .loggedAt(LocalDateTime.now())
                .build();
        workoutLogRepository.save(log);

        // 세트 저장
        for (int i = 0; i < sets.size(); i++) {
            SetRequest set = sets.get(i);
            WorkoutSet workoutSet = WorkoutSet.builder()
                    .workoutLog(log)
                    .setNumber(i + 1)
                    .weightKg(set.weightKg())
                    .reps(set.reps())
                    .isDropset(set.isDropset())
                    .build();
            workoutSetRepository.save(workoutSet);
        }

        // 드롭세트 제외한 일반 세트 수 카운트 → 신발코인 지급
        int normalSetCount = (int) sets.stream().filter(s -> !s.isDropset()).count();
        currencyService.grantShoeCoin(userId, normalSetCount, log.getId());

        return log;
    }

    // 최근 운동 기록 조회
    @Transactional(readOnly = true)
    public List<WorkoutSet> getRecentSets(Long userId, Long routineExerciseId) {
        return workoutLogRepository
                .findTopByUserIdAndRoutineExerciseIdOrderByLoggedAtDesc(userId, routineExerciseId)
                .map(log -> workoutSetRepository.findAllByWorkoutLogIdOrderBySetNumber(log.getId()))
                .orElse(List.of());
    }

    public record SetRequest(BigDecimal weightKg, Integer reps, boolean isDropset) {}
}
