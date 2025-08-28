package com.cpierres.p13.poc.backend.controller;

import com.cpierres.p13.poc.backend.dto.SendMessageRequest;
import com.cpierres.p13.poc.backend.entity.ChatMessage;
import com.cpierres.p13.poc.backend.entity.SupportTicket;
import com.cpierres.p13.poc.backend.entity.TicketStatus;
import com.cpierres.p13.poc.backend.service.ChatMessageService;
import com.cpierres.p13.poc.backend.service.SupportTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Slf4j
public class ChatRestController {
    
    @Autowired
    private SupportTicketService supportTicketService;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    // Support Ticket endpoints
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicket>> getAllTickets() {
        try {
            log.debug("Récupération de tous les tickets");
            List<SupportTicket> tickets = supportTicketService.getAllTickets();
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicket> getTicketById(@PathVariable UUID id) {
        try {
            log.debug("Récupération ticket par ID : {}", id);
            SupportTicket ticket = supportTicketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            log.warn("Ticket non trouvé : {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du ticket {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/tickets/user/{userId}")
    public ResponseEntity<List<SupportTicket>> getTicketsByUserId(@PathVariable UUID userId) {
        try {
            log.debug("Récupération tickets pour utilisateur : {}", userId);
            List<SupportTicket> tickets = supportTicketService.getTicketsByUserId(userId);
            return ResponseEntity.ok(tickets);
        } catch (IllegalArgumentException e) {
            log.warn("Utilisateur non trouvé : {}", userId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets pour l'utilisateur {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/tickets/agent/{assignedAgent}")
    public ResponseEntity<List<SupportTicket>> getTicketsByAssignedAgent(@PathVariable String assignedAgent) {
        try {
            log.debug("Récupération tickets pour agent : {}", assignedAgent);
            List<SupportTicket> tickets = supportTicketService.getTicketsByAssignedAgent(assignedAgent);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets pour l'agent {}", assignedAgent, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<List<SupportTicket>> getTicketsByStatus(@PathVariable TicketStatus status) {
        try {
            log.debug("Récupération tickets par statut : {}", status);
            List<SupportTicket> tickets = supportTicketService.getTicketsByStatus(status);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des tickets par statut {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        try {
            log.info("Création d'un nouveau ticket : {}", ticket.getSubject());
            SupportTicket savedTicket = supportTicketService.createTicket(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTicket);
        } catch (IllegalArgumentException e) {
            log.warn("Données de ticket invalides : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la création du ticket", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/tickets/{id}")
    public ResponseEntity<SupportTicket> updateTicket(@PathVariable UUID id, @RequestBody SupportTicket ticketDetails) {
        try {
            log.info("Mise à jour du ticket : {}", id);
            SupportTicket updatedTicket = supportTicketService.updateTicket(id, ticketDetails);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            log.warn("Ticket non trouvé ou données invalides : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Opération non autorisée : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du ticket {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Chat Message endpoints
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getAllMessages() {
        try {
            log.debug("Récupération de tous les messages");
            List<ChatMessage> messages = chatMessageService.getAllMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des messages", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/messages/{id}")
    public ResponseEntity<ChatMessage> getMessageById(@PathVariable UUID id) {
        try {
            log.debug("Récupération message par ID : {}", id);
            ChatMessage message = chatMessageService.getMessageById(id);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            log.warn("Message non trouvé : {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du message {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/messages/ticket/{ticketId}")
    public ResponseEntity<List<ChatMessage>> getMessagesByTicketId(@PathVariable UUID ticketId) {
        try {
            log.debug("Récupération messages pour ticket : {}", ticketId);
            List<ChatMessage> messages = chatMessageService.getTicketHistory(ticketId);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            log.warn("Ticket non trouvé : {}", ticketId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des messages pour le ticket {}", ticketId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/messages/sender/{senderId}")
    public ResponseEntity<List<ChatMessage>> getMessagesBySenderId(@PathVariable UUID senderId) {
        try {
            log.debug("Récupération messages pour expéditeur : {}", senderId);
            List<ChatMessage> messages = chatMessageService.getMessagesBySenderId(senderId);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            log.warn("Expéditeur non trouvé : {}", senderId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des messages pour l'expéditeur {}", senderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/messages")
    public ResponseEntity<ChatMessage> createMessage(@RequestBody SendMessageRequest request) {
        try {
            log.info("Création d'un nouveau message pour le ticket : {}", request.getTicketId());
            ChatMessage savedMessage = chatMessageService.sendMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
        } catch (IllegalArgumentException e) {
            log.warn("Données de message invalides : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Opération non autorisée : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la création du message", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/tickets/{ticketId}/messages/count")
    public ResponseEntity<Long> getMessageCountByTicket(@PathVariable UUID ticketId) {
        try {
            log.debug("Comptage messages pour ticket : {}", ticketId);
            Long count = chatMessageService.countMessagesByTicket(ticketId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            log.warn("Ticket non trouvé : {}", ticketId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors du comptage des messages pour le ticket {}", ticketId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}