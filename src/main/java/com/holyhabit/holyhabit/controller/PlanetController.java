package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.PlanetResponse;
import com.holyhabit.holyhabit.entity.Planet;
import com.holyhabit.holyhabit.entity.PlanetStage;
import com.holyhabit.holyhabit.repository.PlanetRepository;
import com.holyhabit.holyhabit.repository.PlanetStageRepository;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planets")
@RequiredArgsConstructor
public class PlanetController {

    private final PlanetRepository planetRepository;
    private final PlanetStageRepository planetStageRepository;

    // 공개 행성 목록
    // GET /planets
    @GetMapping
    public ResponseEntity<List<PlanetResponse>> getPlanets(
            @AuthenticationPrincipal CustomUserDetails u) {
        List<Planet> planets = planetRepository.findAllByIsActiveTrueOrderByIdAsc();
        List<PlanetResponse> response = planets.stream()
                .map(planet -> {
                    List<PlanetStage> stages =
                            planetStageRepository.findAllByPlanetIdOrderBySortOrderAsc(
                                    planet.getId());
                    return new PlanetResponse(planet, stages);
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    // 특정 행성 상세 (탐험 구역 포함)
    // GET /planets/{planetId}
    @GetMapping("/{planetId}")
    public ResponseEntity<PlanetResponse> getPlanet(
            @PathVariable Long planetId,
            @AuthenticationPrincipal CustomUserDetails u) {
        Planet planet = planetRepository.findById(planetId)
                .orElseThrow(() -> new RuntimeException("행성을 찾을 수 없습니다."));
        List<PlanetStage> stages =
                planetStageRepository.findAllByPlanetIdOrderBySortOrderAsc(planetId);
        return ResponseEntity.ok(new PlanetResponse(planet, stages));
    }
}