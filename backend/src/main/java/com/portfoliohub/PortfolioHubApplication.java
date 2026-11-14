package com.portfoliohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.Map;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PortfolioHubApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PortfolioHubApplication.class);
        application.setDefaultProperties(Map.of("spring.profiles.default", "local"));
        application.run(args);
    }
}
