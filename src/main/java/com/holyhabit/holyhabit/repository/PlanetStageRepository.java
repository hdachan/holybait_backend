package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.PlanetStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanetStageRepository extends JpaRepository<PlanetStage, Long> {

    // 특정 행성의 탐험 구역 목록 (순서대로)
    List<PlanetStage> findAllByPlanetIdOrderBySortOrderAsc(Long planetId);
}