package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.entity.Exercise;
import com.holyhabit.holyhabit.entity.User;
import com.holyhabit.holyhabit.repository.ExerciseRepository;
import com.holyhabit.holyhabit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    // 앱의 부위 선택지와 같음
    private static final List<String> TARGETS = List.of("하체", "가슴", "등", "어깨", "팔");

    // 전체 운동 목록 조회 (앱 제공 + 내 커스텀)
    @Transactional(readOnly = true)
    public List<Exercise> getExercises(Long userId) {
        return exerciseRepository.findAllAvailable(userId);
    }

    // 카테고리 필터 조회
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesByTarget(Long userId, String target) {
        return exerciseRepository.findByTarget(userId, target);
    }

    // 이름 검색
    @Transactional(readOnly = true)
    public List<Exercise> searchExercises(Long userId, String keyword) {
        return exerciseRepository.searchByName(userId, keyword);
    }

    // 커스텀 운동 추가
    @Transactional
    public Exercise createCustomExercise(Long userId, String name, String target) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Exercise exercise = Exercise.builder()
                .name(name)
                .target(target)
                .isCustom(true)
                .user(user)
                .build();

        return exerciseRepository.save(exercise);
    }

    // 커스텀 운동 수정 (본인 것만). 기록은 운동에 붙어 있어서 예전 기록도 새 이름으로 보임
    @Transactional
    public Exercise updateCustomExercise(Long exerciseId, Long userId, String name, String target) {
        Exercise exercise = getMyCustomExercise(exerciseId, userId);
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("운동 이름을 입력해주세요.");
        }
        if (target == null || !TARGETS.contains(target)) {
            throw new IllegalArgumentException("부위를 선택해주세요.");
        }
        exercise.update(trimmed, target);
        return exercise;
    }

    // 커스텀 운동 삭제 (본인 것만) = 숨기기. 운동 목록에서만 빠지고 예전 기록·루틴은 그대로
    @Transactional
    public void deleteCustomExercise(Long exerciseId, Long userId) {
        getMyCustomExercise(exerciseId, userId).hide();
    }

    private Exercise getMyCustomExercise(Long exerciseId, Long userId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));
        if (!exercise.isCustom() || exercise.getUser() == null
                || !exercise.getUser().getId().equals(userId)) {
            throw new RuntimeException("내가 추가한 운동만 고칠 수 있습니다.");
        }
        return exercise;
    }
}
