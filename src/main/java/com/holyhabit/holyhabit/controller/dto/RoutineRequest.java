package com.holyhabit.holyhabit.controller.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class RoutineRequest {
    private String name;
    private List<Long> exerciseIds; // 기존 루틴 추가/수정용

    // 편집 모드 전체 저장용 (순서 + 슈퍼세트 + 새 운동 포함)
    private List<ExerciseItem> exercises;

    @Getter
    public static class ExerciseItem {
        private Long exerciseId;
        private int orderIndex;
        private Integer supersetGroup;
    }
}
