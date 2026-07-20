package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Planet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanetRepository extends JpaRepository<Planet, Long> {

    // 공개된 행성 목록
    List<Planet> findAllByIsActiveTrueOrderByIdAsc();
}