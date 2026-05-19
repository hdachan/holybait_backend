package com.holyhabit.holyhabit.controller.dto;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String provider;    // GOOGLE
    private String idToken;
    private String deviceInfo;
}
