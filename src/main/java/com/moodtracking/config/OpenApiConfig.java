package com.moodtracking.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    // OpenAPI configuration will be enabled after Maven dependencies are properly resolved
    // Current configuration temporarily disabled due to import resolution issues
    
    /*
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mood Tracking System API")
                        .description("A comprehensive Spring Boot application for mood tracking with JWT authentication, analytics, personalized recommendations, and notification reminders.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mood Tracking Team")
                                .email("support@moodtracking.com")
                                .url("https://moodtracking.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.moodtracking.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token with 'Bearer ' prefix")));
    }
    */
}