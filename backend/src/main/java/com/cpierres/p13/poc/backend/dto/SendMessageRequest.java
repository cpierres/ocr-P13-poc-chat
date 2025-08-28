package com.cpierres.p13.poc.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO pour l'envoi d'un message de chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    private UUID ticketId;
    private UUID senderId;
    private String content;
    
    /**
     * Validation des données requises
     */
    public boolean isValid() {
        return ticketId != null && 
               senderId != null &&
               content != null && !content.trim().isEmpty() &&
               content.length() <= 1000;
    }
}