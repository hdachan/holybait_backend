package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.AppVersionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/app")
public class AppVersionController {

    // ⚠️ 배포 체크리스트
    // 1. pubspec.yaml version 올리기
    // 2. 스토어 심사 통과 확인
    // 3. 이 파일 minVersion/latestVersion 수정
    // 4. 서버 재배포
    // 앱 버전 정보
    // GET /app/version
    @GetMapping("/version")
    public ResponseEntity<AppVersionResponse> getVersion() {
        return ResponseEntity.ok(new AppVersionResponse(
                "1.0.0",   // minVersion: 이 버전 미만이면 강제 업데이트
                "1.0.0",   // latestVersion: 최신 버전
                true        // forceUpdate: true면 강제, false면 권장
        ));
    }
}