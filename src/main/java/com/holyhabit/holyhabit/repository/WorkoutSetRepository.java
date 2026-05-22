package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    // 특정 운동 기록의 세트 목록 조회
    List<WorkoutSet> findAllByWorkoutLogIdOrderBySetNumber(Long workoutLogId);

    // 운동 기록 삭제 시 세트 전체 삭제
    @Modifying
    @Query("DELETE FROM WorkoutSet ws WHERE ws.workoutLog.id = :workoutLogId")
    void deleteAllByWorkoutLogId(Long workoutLogId);
}
