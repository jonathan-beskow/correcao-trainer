package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // permite todas as rotas
                        .allowedOrigins("*") // permite requisições de qualquer origem
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // permite os métodos HTTP
                        .allowedHeaders("*"); // permite todos os headers
            }
        };
    }
}
