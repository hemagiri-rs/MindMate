package com.moodtracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoodTrackingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoodTrackingSystemApplication.class, args);
    }

}