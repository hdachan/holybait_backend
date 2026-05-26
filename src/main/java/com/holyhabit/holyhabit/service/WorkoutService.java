package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    @Transactional
    public SaveResult saveWorkoutLog(
            Long userId,
            Long routineExerciseId,
            List<SetRequest> sets,
            boolean grantCoin
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        RoutineExercise routineExercise = routineExerciseRepository
                .findById(routineExerciseId)
                .orElseThrow(() -> new RuntimeException("루틴 운동을 찾을 수 없습니다."));

        // 오늘 이미 저장한 log 가 있으면 재사용, 없으면 새로 생성
        LocalDate todayKst = LocalDate.now(KST);
        LocalDateTime from = todayKst.atStartOfDay();
        LocalDateTime to = todayKst.plusDays(1).atStartOfDay();

        WorkoutLog log = workoutLogRepository
                .findTodayLog(userId, routineExerciseId, from, to)
                .orElse(null);

        if (log != null) {
            // 기존 오늘 log → 세트 전부 삭제 후 새로 저장 (덮어쓰기)
            workoutSetRepository.deleteAllByWorkoutLogId(log.getId());
        } else {
            // 새 log 생성
            log = WorkoutLog.builder()
                    .user(user)
                    .routineExercise(routineExercise)
                    .loggedAt(LocalDateTime.now())
                    .build();
            workoutLogRepository.save(log);
        }

        for (int i = 0; i < sets.size(); i++) {
            SetRequest set = sets.get(i);
            workoutSetRepository.save(WorkoutSet.builder()
                    .workoutLog(log)
                    .setNumber(i + 1)
                    .weightKg(set.weightKg())
                    .reps(set.reps())
                    .isDropset(set.isDropset())
                    .build());
        }

        int grantedShoeCoin = 0;
        if (grantCoin) {
            int normalSetCount = (int) sets.stream()
                    .filter(s -> !s.isDropset()
                            && s.reps() != null
                            && s.reps() > 0)
                    .count();
            if (normalSetCount > 0) {
                grantedShoeCoin = currencyService.grantShoeCoin(
                        userId, normalSetCount, log.getId(), routineExerciseId);
            }
        }

        return new SaveResult(log, grantedShoeCoin);
    }

    public record SetRequest(BigDecimal weightKg, Integer reps, boolean isDropset) {}
    public record SaveResult(WorkoutLog log, int grantedShoeCoin) {}
}
