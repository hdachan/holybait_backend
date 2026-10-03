package com.holyhabit.holyhabit.service;

// 이미 폐기된 refresh 토큰이 다시 사용됨 (탈취 의심)
// TokenService.refresh 에서 전체 세션 폐기를 커밋한 뒤 던지기 위해 별도 예외로 둠 (noRollbackFor 대상)
public class TokenReuseException extends RuntimeException {
    public TokenReuseException(String message) {
        super(message);
    }
}
