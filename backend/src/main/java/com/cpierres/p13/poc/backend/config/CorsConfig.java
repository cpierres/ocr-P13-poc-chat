package com.cpierres.p13.poc.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuration CORS pour permettre les requêtes cross-origin
 * depuis le frontend Angular vers le backend Spring Boot
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Autoriser les requêtes depuis le frontend Angular
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200"  // Angular dev server
        ));
        
        // Autoriser tous les headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Autoriser toutes les méthodes HTTP
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Autoriser les credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        // Durée de mise en cache des informations CORS (en secondes)
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}