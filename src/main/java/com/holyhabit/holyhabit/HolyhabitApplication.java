package com.holyhabit.holyhabit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HolyhabitApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolyhabitApplication.class, args);
    }
}
