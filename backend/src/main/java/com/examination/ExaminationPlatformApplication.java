package com.examination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ExaminationPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExaminationPlatformApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println();
        System.out.println("============================================");
        System.out.println("   Online Examination Platform Started!");
        System.out.println("============================================");
        System.out.println("   Frontend URL: http://localhost:8080");
        System.out.println("============================================");
        System.out.println();
    }

}