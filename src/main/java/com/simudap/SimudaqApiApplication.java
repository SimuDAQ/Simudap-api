package com.simudap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SimudaqApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimudaqApiApplication.class, args);
    }

}
