package com.holyhabit.holyhabit.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppVersionResponse {
    private String minVersion;
    private String latestVersion;
    private boolean forceUpdate;
}