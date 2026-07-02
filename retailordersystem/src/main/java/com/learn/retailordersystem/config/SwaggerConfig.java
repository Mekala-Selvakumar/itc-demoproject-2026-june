package com.learn.retailordersystem.config;

 
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI retailOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail Order Management API")
                        .version("1.0")
                        .description("API documentation for Retail Order System"));
    }
}