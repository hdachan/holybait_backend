package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.WorkoutHistoryDetailResponse;
import com.holyhabit.holyhabit.controller.dto.WorkoutHistoryResponse;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        LocalDate todayKst = LocalDate.now(KST);
        LocalDateTime from = todayKst.atStartOfDay();
        LocalDateTime to = todayKst.plusDays(1).atStartOfDay();

        WorkoutLog log = workoutLogRepository
                .findTodayLog(userId, routineExerciseId, from, to)
                .orElse(null);

        if (log != null) {
            workoutSetRepository.deleteAllByWorkoutLogId(log.getId());
        } else {
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

    // 내가 운동한 종목 목록
    @Transactional(readOnly = true)
    public List<WorkoutHistoryResponse> getExerciseHistory(Long userId) {
        List<WorkoutLog> logs = workoutLogRepository
                .findAllByUserIdOrderByLoggedAtDesc(userId);

        // exerciseId 기준으로 그룹핑
        Map<Long, List<WorkoutLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getRoutineExercise().getExercise().getId()));

        List<WorkoutHistoryResponse> result = new ArrayList<>();
        for (Map.Entry<Long, List<WorkoutLog>> entry : grouped.entrySet()) {
            WorkoutLog latest = entry.getValue().get(0); // 최신 기록
            Exercise exercise = latest.getRoutineExercise().getExercise();

            result.add(new WorkoutHistoryResponse(
                    exercise.getId(),
                    exercise.getName(),
                    exercise.getTarget(),
                    latest.getLoggedAt().toLocalDate().format(DATE_FMT),
                    entry.getValue().size()
            ));
        }

        // 최근 운동일 기준 정렬
        result.sort((a, b) -> b.getLastLoggedDate().compareTo(a.getLastLoggedDate()));
        return result;
    }

    // 특정 종목 날짜별 기록
    @Transactional(readOnly = true)
    public List<WorkoutHistoryDetailResponse> getExerciseDetail(
            Long userId, Long exerciseId) {

        List<WorkoutLog> logs = workoutLogRepository
                .findAllByUserIdAndExerciseIdOrderByLoggedAtDesc(userId, exerciseId);

        List<WorkoutHistoryDetailResponse> result = new ArrayList<>();
        for (WorkoutLog log : logs) {
            List<WorkoutSet> sets = workoutSetRepository
                    .findAllByWorkoutLogIdOrderBySetNumber(log.getId());

            int totalSets = (int) sets.stream()
                    .filter(s -> !s.isDropset()).count();

            List<WorkoutHistoryDetailResponse.SetDetail> setDetails = sets.stream()
                    .map(s -> new WorkoutHistoryDetailResponse.SetDetail(
                            s.getSetNumber(),
                            s.getWeightKg() != null ? s.getWeightKg().doubleValue() : null,
                            s.getReps(),
                            s.isDropset()
                    ))
                    .toList();

            result.add(new WorkoutHistoryDetailResponse(
                    log.getLoggedAt().toLocalDate().format(DATE_FMT),
                    totalSets,
                    setDetails
            ));
        }
        return result;
    }

    public record SetRequest(BigDecimal weightKg, Integer reps, boolean isDropset) {}
    public record SaveResult(WorkoutLog log, int grantedShoeCoin) {}
}