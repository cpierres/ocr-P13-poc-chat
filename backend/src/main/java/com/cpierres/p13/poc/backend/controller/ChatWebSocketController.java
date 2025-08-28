package com.cpierres.p13.poc.backend.controller;

import com.cpierres.p13.poc.backend.dto.SendMessageRequest;
import com.cpierres.p13.poc.backend.entity.ChatMessage;
import com.cpierres.p13.poc.backend.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@Slf4j
public class ChatWebSocketController {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Gérer les messages de chat entrants et les diffuser aux abonnés du ticket
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages/{ticketId}")
    public ChatMessage sendMessage(ChatMessage message) {
        try {
            log.info("Réception message WebSocket pour ticket {} de l'utilisateur {}", 
                    message.getTicketId(), message.getSenderId());
            
            // Créer le DTO à partir du message WebSocket
            SendMessageRequest request = new SendMessageRequest(
                message.getTicketId(),
                message.getSenderId(),
                message.getContent()
            );
            
            // Utiliser le service pour envoyer le message avec toute la logique métier
            ChatMessage savedMessage = chatMessageService.sendMessage(request);
            
            log.info("Message WebSocket traité avec succès : {} (ID: {})", 
                    savedMessage.getContent(), savedMessage.getId());
            
            return savedMessage;
        } catch (IllegalArgumentException e) {
            log.error("Données de message WebSocket invalides : {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.error("Opération WebSocket non autorisée : {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message WebSocket", e);
            throw e;
        }
    }
    
    /**
     * Gérer l'arrivée d'un utilisateur dans une conversation de ticket
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/users/{ticketId}")
    public String userJoined(@DestinationVariable String ticketId, 
                           SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Utilisateur connecté au ticket {} (session: {})", ticketId, sessionId);
        
        // Stocker les informations de session utilisateur si nécessaire
        headerAccessor.getSessionAttributes().put("ticketId", ticketId);
        
        return "Utilisateur connecté au ticket " + ticketId;
    }
    
    /**
     * Gérer le départ d'un utilisateur d'une conversation de ticket
     */
    @MessageMapping("/chat.leave")
    @SendTo("/topic/users/{ticketId}")
    public String userLeft(@DestinationVariable String ticketId,
                          SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Utilisateur déconnecté du ticket {} (session: {})", ticketId, sessionId);
        
        return "Utilisateur déconnecté du ticket " + ticketId;
    }
    
    /**
     * Envoyer un indicateur de saisie
     */
    @MessageMapping("/chat.typing")
    public void userTyping(@DestinationVariable String ticketId,
                          SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("Utilisateur en train d'écrire dans le ticket {} (session: {})", ticketId, sessionId);
        
        // Diffuser l'indicateur de saisie aux autres utilisateurs participant au ticket
        messagingTemplate.convertAndSend(
            "/topic/typing/" + ticketId, 
            "Un utilisateur est en train d'écrire..."
        );
    }
}