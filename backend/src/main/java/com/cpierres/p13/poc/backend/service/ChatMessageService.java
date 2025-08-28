package com.cpierres.p13.poc.backend.service;

import com.cpierres.p13.poc.backend.dto.MockUserInfo;
import com.cpierres.p13.poc.backend.dto.SendMessageRequest;
import com.cpierres.p13.poc.backend.dto.SystemMessageRequest;
import com.cpierres.p13.poc.backend.entity.ChatMessage;
import com.cpierres.p13.poc.backend.entity.SupportTicket;
import com.cpierres.p13.poc.backend.entity.TicketStatus;
import com.cpierres.p13.poc.backend.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class ChatMessageService {
    
    @Autowired
    private ChatMessageRepository messageRepository;
    
    @Autowired
    private SupportTicketService ticketService;
    
//    @Autowired
//    private UserService userService;

    // Au lieu de UserService
    private final SupportUserService supportUserService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatMessageService(SupportUserService supportUserService) {
        this.supportUserService = supportUserService;
    }

    /**
     * Envoyer un message de chat avec validation et notification temps réel
     */
    public ChatMessage sendMessage(SendMessageRequest request) {
        log.info("Envoi d'un message pour le ticket {} par l'utilisateur {}", request.getTicketId(), request.getSenderId());
        
        // Validation DTO
        if (!request.isValid()) {
            throw new IllegalArgumentException("Données de message invalides");
        }
        
        // Validation métier
        validateMessage(request.getTicketId(), request.getSenderId(), request.getContent());
        
        // Validation ticket exists et est accessible
        SupportTicket ticket = ticketService.getTicketById(request.getTicketId());
        
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalStateException("Impossible d'envoyer un message dans un ticket fermé");
        }
        
        // Validation utilisateur existe
        //User sender = userService.getUserById(request.getSenderId());
        MockUserInfo sender = supportUserService.getChatUserInfo(request.getSenderId());
        log.debug("Message envoyé par : {} ({})", sender.getEmail(), sender.getRole());
        
        // Création du message
        ChatMessage message = new ChatMessage();
        message.setTicketId(request.getTicketId());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        
        ChatMessage savedMessage = messageRepository.save(message);
        
        // Mise à jour statut ticket si nécessaire
        updateTicketStatusIfNeeded(ticket, savedMessage);
        
        // Notification temps réel
        sendRealTimeNotification(savedMessage);
        
        log.info("Message envoyé avec succès dans le ticket {} (ID message: {})", request.getTicketId(), savedMessage.getId());
        
        return savedMessage;
    }
    
    /**
     * Récupérer l'historique des messages d'un ticket
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getTicketHistory(UUID ticketId) {
        log.debug("Récupération de l'historique du ticket : {}", ticketId);
        
        // Validation accès ticket
        ticketService.getTicketById(ticketId); // Lève exception si pas d'accès
        
        return messageRepository.findByTicketIdOrderByTimestampAsc(ticketId);
    }
    
    /**
     * Récupérer tous les messages
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getAllMessages() {
        log.debug("Récupération de tous les messages");
        return messageRepository.findAll();
    }
    
    /**
     * Récupérer un message par ID
     */
    @Transactional(readOnly = true)
    public ChatMessage getMessageById(UUID id) {
        log.debug("Récupération message par ID : {}", id);
        
        return messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Message non trouvé avec l'ID : {}", id);
                    return new IllegalArgumentException("Message non trouvé avec l'ID : " + id);
                });
    }
    
    /**
     * Récupérer les messages d'un expéditeur
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesBySenderId(UUID senderId) {
        log.debug("Récupération messages pour l'expéditeur : {}", senderId);
        
        // Vérifier que l'utilisateur existe
        supportUserService.getChatUserInfo(senderId);
        
        return messageRepository.findBySenderId(senderId);
    }
    
    /**
     * Récupérer les messages récents d'un ticket
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getRecentMessages(UUID ticketId, LocalDateTime since) {
        log.debug("Récupération messages récents du ticket {} depuis {}", ticketId, since);
        
        // Validation accès ticket
        ticketService.getTicketById(ticketId);
        
        return messageRepository.findByTicketIdAndTimestampAfter(ticketId, since);
    }
    
    /**
     * Compter les messages d'un ticket
     */
    @Transactional(readOnly = true)
    public Long countMessagesByTicket(UUID ticketId) {
        log.debug("Comptage messages pour le ticket : {}", ticketId);
        
        // Validation accès ticket
        ticketService.getTicketById(ticketId);
        
        return messageRepository.countByTicketId(ticketId);
    }
    
    /**
     * Envoyer un message système automatique
     */
    public ChatMessage sendSystemMessage(SystemMessageRequest request) {
        log.info("Envoi d'un message système pour le ticket : {}", request.getTicketId());
        
        // Validation DTO
        if (!request.isValid()) {
            throw new IllegalArgumentException("Données de message système invalides");
        }
        
        // Validation ticket exists
        SupportTicket ticket = ticketService.getTicketById(request.getTicketId());
        
        // Création message système (sans expéditeur spécifique)
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setTicketId(request.getTicketId());
        systemMessage.setSenderId(null); // Message système
        systemMessage.setContent("[SYSTÈME] " + request.getContent());
        
        ChatMessage savedMessage = messageRepository.save(systemMessage);
        
        // Notification temps réel
        sendRealTimeNotification(savedMessage);
        
        log.info("Message système envoyé dans le ticket {} (ID message: {})", request.getTicketId(), savedMessage.getId());
        
        return savedMessage;
    }
    
    /**
     * Méthode de compatibilité - Envoyer un message système automatique
     * @deprecated Utiliser sendSystemMessage(SystemMessageRequest) à la place
     */
    @Deprecated
    public ChatMessage sendSystemMessage(UUID ticketId, String content) {
        SystemMessageRequest request = new SystemMessageRequest(ticketId, content);
        return sendSystemMessage(request);
    }
    
    /**
     * Marquer qu'un utilisateur est en train d'écrire
     */
    public void notifyUserTyping(UUID ticketId, UUID userId) {
        log.debug("Notification saisie en cours - Ticket: {}, Utilisateur: {}", ticketId, userId);
        
        // Validation ticket et utilisateur existent
        ticketService.getTicketById(ticketId);
        MockUserInfo user = supportUserService.getChatUserInfo(userId);
        
        // Notification temps réel de saisie
        String typingMessage = user.getFirstName() + " est en train d'écrire...";
        messagingTemplate.convertAndSend("/topic/typing/" + ticketId, typingMessage);
    }
    
    /**
     * Notifier qu'un utilisateur a rejoint la conversation
     */
    public void notifyUserJoined(UUID ticketId, UUID userId) {
        log.info("Utilisateur {} a rejoint la conversation du ticket {}", userId, ticketId);
        
        // Validation ticket et utilisateur existent
        ticketService.getTicketById(ticketId);
        MockUserInfo user = supportUserService.getChatUserInfo(userId);
        
        // Message système automatique
        String joinMessage = user.getFirstName() + " " + user.getLastName() + " a rejoint la conversation";
        sendSystemMessage(ticketId, joinMessage);
        
        // Notification temps réel
        messagingTemplate.convertAndSend("/topic/users/" + ticketId, 
            "Utilisateur connecté: " + user.getFirstName());
    }
    
    /**
     * Notifier qu'un utilisateur a quitté la conversation
     */
    public void notifyUserLeft(UUID ticketId, UUID userId) {
        log.info("Utilisateur {} a quitté la conversation du ticket {}", userId, ticketId);
        
        try {
            // Validation ticket et utilisateur existent
            ticketService.getTicketById(ticketId);
            MockUserInfo user = supportUserService.getChatUserInfo(userId);
            
            // Message système automatique
            String leaveMessage = user.getFirstName() + " " + user.getLastName() + " a quitté la conversation";
            sendSystemMessage(ticketId, leaveMessage);
            
            // Notification temps réel
            messagingTemplate.convertAndSend("/topic/users/" + ticketId, 
                "Utilisateur déconnecté: " + user.getFirstName());
        } catch (Exception e) {
            log.warn("Erreur lors de la notification de départ utilisateur: {}", e.getMessage());
        }
    }
    
    /**
     * Obtenir les statistiques de messages pour un ticket
     */
    @Transactional(readOnly = true)
    public MessageStats getTicketMessageStats(UUID ticketId) {
        log.debug("Calcul statistiques messages pour ticket : {}", ticketId);
        
        // Validation accès ticket
        ticketService.getTicketById(ticketId);
        
        List<ChatMessage> messages = messageRepository.findByTicketId(ticketId);
        
        long totalMessages = messages.size();
        long systemMessages = messages.stream()
                .filter(msg -> msg.getSenderId() == null)
                .count();
        long userMessages = totalMessages - systemMessages;
        
        LocalDateTime firstMessageTime = messages.isEmpty() ? null : 
            messages.get(0).getTimestamp();
        LocalDateTime lastMessageTime = messages.isEmpty() ? null : 
            messages.get(messages.size() - 1).getTimestamp();
        
        return new MessageStats(totalMessages, userMessages, systemMessages, 
                               firstMessageTime, lastMessageTime);
    }
    
    /**
     * Mettre à jour le statut du ticket si nécessaire après envoi de message
     */
    private void updateTicketStatusIfNeeded(SupportTicket ticket, ChatMessage message) {
        // Si c'est le premier message client et que le ticket est OPEN, passer en IN_PROGRESS
        if (ticket.getStatus() == TicketStatus.OPEN && message.getSenderId() != null) {
            try {
                //User sender = userService.getUserById(message.getSenderId());
                // Validation de l'utilisateur via le service mock
                MockUserInfo sender = supportUserService.getChatUserInfo(message.getSenderId());

                if (sender.getRole().equals("CLIENT")) {
                    log.info("Premier message client reçu, passage du ticket {} en IN_PROGRESS", ticket.getId());
                    ticket.setStatus(TicketStatus.IN_PROGRESS);
                    // Note: Éviter la boucle en appelant directement le repository
                    // ticketService.updateTicket(ticket.getId(), ticket);
                }
            } catch (Exception e) {
                log.warn("Erreur lors de la mise à jour du statut du ticket: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Envoyer notification temps réel via WebSocket
     */
    private void sendRealTimeNotification(ChatMessage message) {
        try {
            log.debug("Envoi notification temps réel pour message ID: {}", message.getId());
            
            // Diffuser le message à tous les abonnés du ticket
            messagingTemplate.convertAndSend(
                "/topic/messages/" + message.getTicketId(), 
                message
            );
            
            // Notification globale (optionnel, pour dashboard admin)
            messagingTemplate.convertAndSend(
                "/topic/notifications", 
                "Nouveau message dans ticket " + message.getTicketId()
            );
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de notification temps réel pour message {}: {}", 
                     message.getId(), e.getMessage());
        }
    }
    
    /**
     * Validation métier des données de message
     */
    private void validateMessage(UUID ticketId, UUID senderId, String content) {
        if (ticketId == null) {
            throw new IllegalArgumentException("L'ID du ticket est obligatoire");
        }
        
        if (senderId == null) {
            throw new IllegalArgumentException("L'ID de l'expéditeur est obligatoire");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du message est obligatoire");
        }
        
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Le message ne peut pas dépasser 1000 caractères");
        }
        
        log.debug("Validation message réussie pour ticket : {}", ticketId);
    }
    
    /**
     * Classe interne pour les statistiques de messages
     */
    public static class MessageStats {
        private final long totalMessages;
        private final long userMessages;
        private final long systemMessages;
        private final LocalDateTime firstMessageTime;
        private final LocalDateTime lastMessageTime;
        
        public MessageStats(long totalMessages, long userMessages, long systemMessages,
                           LocalDateTime firstMessageTime, LocalDateTime lastMessageTime) {
            this.totalMessages = totalMessages;
            this.userMessages = userMessages;
            this.systemMessages = systemMessages;
            this.firstMessageTime = firstMessageTime;
            this.lastMessageTime = lastMessageTime;
        }
        
        // Getters
        public long getTotalMessages() { return totalMessages; }
        public long getUserMessages() { return userMessages; }
        public long getSystemMessages() { return systemMessages; }
        public LocalDateTime getFirstMessageTime() { return firstMessageTime; }
        public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    }
}