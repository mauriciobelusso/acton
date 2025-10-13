package dev.acton.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.acton")
public class SampleApp {
    public static void main(String[] args) {
        SpringApplication.run(SampleApp.class, args);
    }
}
