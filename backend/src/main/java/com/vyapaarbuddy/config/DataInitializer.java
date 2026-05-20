package com.vyapaarbuddy.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Data initializer for development and testing.
 * TODO: Implement data seeding for initial roles and default admin user
 * TODO: Add sample business data for development
 */
@Configuration
public class DataInitializer {

    @Bean
    @Profile({"dev", "test"})
    public CommandLineRunner initData() {
        return args -> {
            // TODO: Initialize default data for development
            // - Create default roles (USER, ADMIN)
            // - Create default admin user
            // - Create sample business data
            System.out.println("Data initialization placeholder - implement in next phase");
        };
    }
}
