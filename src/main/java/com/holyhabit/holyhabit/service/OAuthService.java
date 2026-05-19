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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class OAuthService {

    private final GoogleIdTokenVerifier verifier;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OAuthService(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        ).setAudience(Collections.singletonList(clientId)).build();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleUserInfo verifyGoogleToken(String token) {
        // idToken 검증 먼저 시도
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

        // accessToken으로 Google userinfo API 호출
        try {
            log.info("accessToken으로 userinfo 호출 시도");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            log.info("userinfo 응답: {}", response.body());

            Map<?, ?> data = objectMapper.readValue(response.body(), Map.class);

            String sub   = (String) data.get("sub");
            String email = (String) data.get("email");
            String name  = (String) data.get("name");

            if (sub == null) throw new RuntimeException("sub 없음");

            return new GoogleUserInfo(sub, email, name);
        } catch (Exception e) {
            log.error("userinfo 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Google 로그인 실패");
        }
    }

    public record GoogleUserInfo(String providerId, String email, String name) {}
}