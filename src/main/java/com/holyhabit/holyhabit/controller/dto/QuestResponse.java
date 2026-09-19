package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestResponse {
    private Long questId;
    private String title;
    private String description;
    private String actionType;
    private Integer progress;
    private Integer targetCount;
    private String status;      // in_progress / completed / claimed
    private Integer rewardGold;
    private Integer rewardExp;
}