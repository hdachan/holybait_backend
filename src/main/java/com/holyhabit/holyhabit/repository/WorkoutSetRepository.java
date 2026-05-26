package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    List<WorkoutSet> findAllByWorkoutLogIdOrderBySetNumber(Long workoutLogId);

    @Modifying
    @Query("DELETE FROM WorkoutSet ws WHERE ws.workoutLog.id = :workoutLogId")
    void deleteAllByWorkoutLogId(Long workoutLogId);
}
