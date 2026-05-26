package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    Optional<WorkoutLog> findTopByUserIdAndRoutineExerciseIdOrderByLoggedAtDesc(
            Long userId, Long routineExerciseId);

    List<WorkoutLog> findAllByRoutineExerciseId(Long routineExerciseId);

    @Modifying
    @Query("DELETE FROM WorkoutLog w WHERE w.routineExercise.id = :routineExerciseId")
    void deleteAllByRoutineExerciseId(Long routineExerciseId);
}
