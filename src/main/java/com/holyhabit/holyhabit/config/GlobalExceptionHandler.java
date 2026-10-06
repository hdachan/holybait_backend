package com.holyhabit.holyhabit.config;

import com.holyhabit.holyhabit.service.TokenReuseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

// 서비스에서 던진 에러를 앱이 읽을 수 있는 {code, message} 로 변환
// - 우리가 직접 적은 사유(RuntimeException / IllegalArgumentException) → 400 + 그 문구
// - 인증 코드("401_003" 등) → 해당 상태 + code
// - 그 외 예상 못 한 에러(DB 오류 등) → 500 + 일반 문구 (내부 정보 숨김)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_MESSAGE = "일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요.";

    // 서비스가 메시지로 던지는 인증 관련 코드 → (상태, 사용자 문구)
    private static final Map<String, Map.Entry<HttpStatus, String>> AUTH_CODES = Map.of(
            "401_003", Map.entry(HttpStatus.UNAUTHORIZED, "세션이 만료됐습니다. 다시 로그인해주세요."),
            "401_004", Map.entry(HttpStatus.UNAUTHORIZED, "비정상적인 접근이 감지됐습니다. 다시 로그인해주세요."),
            "403_001", Map.entry(HttpStatus.FORBIDDEN, "정지된 계정입니다. 고객센터에 문의해주세요."),
            "403_002", Map.entry(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다.")
    );

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();

        // 인증 코드
        if (message != null && AUTH_CODES.containsKey(message)) {
            Map.Entry<HttpStatus, String> auth = AUTH_CODES.get(message);
            return body(auth.getKey(), message, auth.getValue());
        }

        // 우리가 직접 적은 사유 — RuntimeException 그 자체와 IllegalArgumentException 만
        // (DB·프레임워크 에러는 RuntimeException 의 하위 종류라 여기 해당하지 않음)
        boolean isOurMessage = ex.getClass() == RuntimeException.class
                || ex.getClass() == IllegalArgumentException.class
                || ex instanceof TokenReuseException;
        if (isOurMessage && message != null && !message.isBlank()) {
            log.info("요청 거부: {}", message);
            return body(HttpStatus.BAD_REQUEST, "400_000", message);
        }

        log.error("처리되지 않은 에러", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "500_000", GENERIC_MESSAGE);
    }

    // 요청 본문 형식 오류 / 경로 값 형식 오류 → 원래처럼 400 유지
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "400_001", "요청 형식이 올바르지 않습니다.");
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
