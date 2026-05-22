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

    // 커스텀 운동 삭제 (본인 것만)
    @Transactional
    public void deleteCustomExercise(Long exerciseId, Long userId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("운동을 찾을 수 없습니다."));

        if (!exercise.isCustom() || !exercise.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        exerciseRepository.deleteById(exerciseId);
    }
}
