package com.learn.employeemanagement.config;

 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public String applicationName() {
        return "Employee Management";
    }

}