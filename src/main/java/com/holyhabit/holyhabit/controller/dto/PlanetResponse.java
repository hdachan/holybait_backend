package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.Planet;
import com.holyhabit.holyhabit.entity.PlanetStage;
import com.holyhabit.holyhabit.entity.Stage;
import lombok.Getter;

import java.util.List;

@Getter
public class PlanetResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageKey;
    private final String theme;
    private final String interviewPersonName;
    private final String interviewPersonJob;
    private final List<StageInfo> stages;

    public PlanetResponse(Planet planet, List<PlanetStage> planetStages) {
        this.id = planet.getId();
        this.name = planet.getName();
        this.description = planet.getDescription();
        this.imageKey = planet.getImageKey();
        this.theme = planet.getTheme();
        this.interviewPersonName = planet.getInterviewPersonName();
        this.interviewPersonJob = planet.getInterviewPersonJob();
        this.stages = planetStages.stream()
                .map(ps -> new StageInfo(ps.getStage(), ps.getSortOrder()))
                .toList();
    }

    @Getter
    public static class StageInfo {
        private final Long id;
        private final String name;
        private final String description;
        private final int minLevel;
        private final int maxLevel;
        private final int shoeCoinCost;
        private final String imageKey;
        private final int sortOrder;

        public StageInfo(Stage stage, int sortOrder) {
            this.id = stage.getId();
            this.name = stage.getName();
            this.description = stage.getDescription();
            this.minLevel = stage.getMinLevel();
            this.maxLevel = stage.getMaxLevel();
            this.shoeCoinCost = stage.getShoeCoinCost();
            this.imageKey = stage.getImageKey();
            this.sortOrder = sortOrder;
        }
    }
}