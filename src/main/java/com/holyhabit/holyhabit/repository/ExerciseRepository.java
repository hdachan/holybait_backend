package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // 앱 제공 운동 + 내 커스텀 운동 전체 조회
    @Query("SELECT e FROM Exercise e WHERE e.isCustom = false OR e.user.id = :userId")
    List<Exercise> findAllAvailable(Long userId);

    // 카테고리 필터 조회
    @Query("SELECT e FROM Exercise e WHERE (e.isCustom = false OR e.user.id = :userId) AND e.target = :target")
    List<Exercise> findByTarget(Long userId, String target);

    // 이름 검색
    @Query("SELECT e FROM Exercise e WHERE (e.isCustom = false OR e.user.id = :userId) AND e.name LIKE %:keyword%")
    List<Exercise> searchByName(Long userId, String keyword);
}
