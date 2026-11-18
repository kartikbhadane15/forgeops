package com.forgeops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class ForgeOpsApplication {

    public static void main(String[] args) {
        // Enforce UTC timezone globally
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ForgeOpsApplication.class, args);
    }
}
