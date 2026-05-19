package com.holyhabit.holyhabit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holyhabit.holyhabit.entity.User;
import com.holyhabit.holyhabit.entity.UserStatus;
import com.holyhabit.holyhabit.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtProvider.JwtValidationResult result = jwtProvider.validate(token);

        switch (result) {
            case EXPIRED -> {
                writeError(response, 401, "401_001", "Access token has expired");
                return;
            }
            case INVALID -> {
                writeError(response, 401, "401_002", "Access token is invalid");
                return;
            }
            case VALID -> {
                Long userId = jwtProvider.getUserId(token);
                User user = userRepository.findById(userId).orElse(null);

                if (user == null) {
                    writeError(response, 401, "401_002", "User not found");
                    return;
                }

                // BANNED / DELETED 유저는 JWT 유효해도 차단
                if (user.getStatus() == UserStatus.BANNED) {
                    writeError(response, 403, "403_001", "User is banned");
                    return;
                }
                if (user.getStatus() == UserStatus.DELETED) {
                    writeError(response, 403, "403_002", "User is deleted");
                    return;
                }

                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status,
                            String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of("code", code, "message", message))
        );
    }
}
