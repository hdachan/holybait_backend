package com.holyhabit.holyhabit.controller.dto;

import lombok.Getter;

@Getter
public class TokenRefreshRequest {
    private String refreshToken;
    private String deviceInfo;
}
