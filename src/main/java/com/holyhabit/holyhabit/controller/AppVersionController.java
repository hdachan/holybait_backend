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
    // 1. pubspec.yaml version 올리기 (매번 필수)
    // 2. 스토어 심사 통과 + 스토어에서 새 버전이 보이는지 확인
    // 3. 이 파일 latestVersion 수정 (강제 업데이트가 필요할 때만)
    // 4. 서버 재배포
    //
    // 앱의 업데이트 규칙 (splash_screen.dart)
    // - latestVersion의 앞자리·가운데 자리가 앱보다 높으면 강제 업데이트 (1.0.x → 1.1.0)
    // - 마지막 자리만 높으면 팝업 없음 (1.0.0 → 1.0.3)
    // - minVersion 미만이면 강제 (마지막 자리 업데이트를 꼭 강제해야 할 때)
    // - forceUpdate 값은 앱에서 쓰지 않음
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