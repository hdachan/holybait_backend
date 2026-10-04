package com.holyhabit.holyhabit.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OAuthService {

    private final GoogleIdTokenVerifier verifier;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // 우리 앱으로 발급된 토큰만 허용할 클라이언트 ID 목록
    // google.allowed-client-ids 를 쉼표로 여러 개 지정 가능 (없으면 google.client-id 하나)
    private final List<String> allowedClientIds;

    public OAuthService(
            @Value("${google.allowed-client-ids:${google.client-id}}") String allowedClientIds) {
        this.allowedClientIds = java.util.Arrays.stream(allowedClientIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        ).setAudience(this.allowedClientIds).build();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleUserInfo verifyGoogleToken(String token) {
        // idToken 검증 먼저 시도 (aud 가 우리 클라이언트 ID 인지 라이브러리가 확인)
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                return new GoogleUserInfo(
                        payload.getSubject(),
                        payload.getEmail(),
                        (String) payload.get("name")
                );
            }
        } catch (Exception e) {
            log.debug("idToken 검증 실패: {}", e.getMessage());
        }

        // accessToken 경로 (웹 로그인은 주로 여기로 옴)
        try {
            // 1) tokeninfo 로 이 토큰이 어느 앱(클라이언트 ID)에 발급됐는지 확인
            //    → 다른 사이트가 받은 구글 토큰으로 우리 앱에 로그인하는 것 차단
            Map<?, ?> tokenInfo = getJson(
                    "https://oauth2.googleapis.com/tokeninfo?access_token="
                            + URLEncoder.encode(token, StandardCharsets.UTF_8),
                    null);
            String audience = (String) tokenInfo.get("aud");
            String authorizedParty = (String) tokenInfo.get("azp");
            if (!allowedClientIds.contains(audience) && !allowedClientIds.contains(authorizedParty)) {
                log.warn("다른 앱에 발급된 구글 토큰으로 로그인 시도 aud={} azp={}",
                        audience, authorizedParty);
                throw new RuntimeException("허용되지 않은 클라이언트의 토큰");
            }

            // 2) userinfo 로 사용자 정보 조회
            Map<?, ?> data = getJson(
                    "https://www.googleapis.com/oauth2/v3/userinfo", "Bearer " + token);

            String sub   = (String) data.get("sub");
            String email = (String) data.get("email");
            String name  = (String) data.get("name");

            if (sub == null) throw new RuntimeException("sub 없음");
            if (!sub.equals(tokenInfo.get("sub"))) throw new RuntimeException("sub 불일치");

            return new GoogleUserInfo(sub, email, name);
        } catch (Exception e) {
            log.error("Google accessToken 검증 실패: {}", e.getMessage());
            throw new RuntimeException("Google 로그인 실패");
        }
    }

    private Map<?, ?> getJson(String url, String authorization) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        if (authorization != null) builder.header("Authorization", authorization);

        HttpResponse<String> response = httpClient.send(
                builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Google API 응답 " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), Map.class);
    }

    public record GoogleUserInfo(String providerId, String email, String name) {}
}
