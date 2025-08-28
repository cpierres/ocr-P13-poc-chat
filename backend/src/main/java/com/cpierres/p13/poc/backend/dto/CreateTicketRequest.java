package com.cpierres.p13.poc.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO pour la création d'un ticket de support
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    
    private UUID userId;
    private String subject;
    private String description;
    
    /**
     * Validation des données requises
     */
    public boolean isValid() {
        return userId != null && 
               subject != null && !subject.trim().isEmpty() &&
               subject.length() <= 255;
    }
}