package com.holyhabit.holyhabit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// 로그인은 JWT(JwtFilter)로만 처리 → Spring 기본 계정(user + 임시 비밀번호) 생성을 끔
// (켜져 있으면 시작할 때마다 로그에 "Using generated security password"가 찍힘)
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
public class HolyhabitApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolyhabitApplication.class, args);
    }
}
