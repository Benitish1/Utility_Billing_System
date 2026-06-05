package com.wasac.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UtilityBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilityBillingApplication.class, args);
    }
}
