package com.cpierres.p13.poc.backend.service;

import com.cpierres.p13.poc.backend.entity.SupportTicket;
import com.cpierres.p13.poc.backend.entity.TicketStatus;
import com.cpierres.p13.poc.backend.entity.User;
import com.cpierres.p13.poc.backend.entity.UserRole;
import com.cpierres.p13.poc.backend.repository.SupportTicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class SupportTicketService {
    
    @Autowired
    private SupportTicketRepository ticketRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Créer un nouveau ticket de support avec validation métier
     */
    public SupportTicket createTicket(SupportTicket ticket) {
        log.info("Création d'un ticket de support : {}", ticket.getSubject());
        
        // Validation métier
        validateTicket(ticket);
        
        // Validation utilisateur
        User user = userService.getUserById(ticket.getUserId());
        log.debug("Ticket créé pour l'utilisateur : {} ({})", user.getEmail(), user.getRole());
        
        // Auto-assignation si possible
        if (ticket.getAssignedAgent() == null) {
            assignAvailableAgent(ticket);
        }
        
        // Définir le statut initial si non défini
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.OPEN);
        }
        
        SupportTicket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket de support créé avec succès : {} (ID: {})", savedTicket.getSubject(), savedTicket.getId());
        
        return savedTicket;
    }
    
    /**
     * Récupérer tous les tickets
     */
    @Transactional(readOnly = true)
    public List<SupportTicket> getAllTickets() {
        log.debug("Récupération de tous les tickets de support");
        return ticketRepository.findAll();
    }
    
    /**
     * Récupérer un ticket par ID
     */
    @Transactional(readOnly = true)
    public SupportTicket getTicketById(UUID id) {
        log.debug("Récupération ticket par ID : {}", id);
        
        return ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ticket non trouvé avec l'ID : {}", id);
                    return new IllegalArgumentException("Ticket non trouvé avec l'ID : " + id);
                });
    }
    
    /**
     * Récupérer les tickets d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<SupportTicket> getTicketsByUserId(UUID userId) {
        log.debug("Récupération tickets pour utilisateur ID : {}", userId);
        
        // Vérifier que l'utilisateur existe
        userService.getUserById(userId);
        
        return ticketRepository.findByUserId(userId);
    }
    
    /**
     * Récupérer les tickets assignés à un agent
     */
    @Transactional(readOnly = true)
    public List<SupportTicket> getTicketsByAssignedAgent(String assignedAgent) {
        log.debug("Récupération tickets pour agent : {}", assignedAgent);
        return ticketRepository.findByAssignedAgent(assignedAgent);
    }
    
    /**
     * Récupérer les tickets par statut
     */
    @Transactional(readOnly = true)
    public List<SupportTicket> getTicketsByStatus(TicketStatus status) {
        log.debug("Récupération tickets par statut : {}", status);
        return ticketRepository.findByStatus(status);
    }
    
    /**
     * Mettre à jour un ticket
     */
    public SupportTicket updateTicket(UUID id, SupportTicket ticketDetails) {
        log.info("Mise à jour ticket ID : {}", id);
        
        SupportTicket existingTicket = getTicketById(id);
        
        // Validation des règles métier pour les changements de statut
        validateStatusTransition(existingTicket.getStatus(), ticketDetails.getStatus());
        
        // Mise à jour des champs
        if (ticketDetails.getSubject() != null) {
            existingTicket.setSubject(ticketDetails.getSubject());
        }
        
        if (ticketDetails.getDescription() != null) {
            existingTicket.setDescription(ticketDetails.getDescription());
        }
        
        if (ticketDetails.getStatus() != null) {
            existingTicket.setStatus(ticketDetails.getStatus());
        }
        
        if (ticketDetails.getAssignedAgent() != null) {
            existingTicket.setAssignedAgent(ticketDetails.getAssignedAgent());
        }
        
        SupportTicket updatedTicket = ticketRepository.save(existingTicket);
        log.info("Ticket mis à jour : {} (ID: {})", updatedTicket.getSubject(), updatedTicket.getId());
        
        return updatedTicket;
    }
    
    /**
     * Assigner un ticket à un agent
     */
    public SupportTicket assignToAgent(UUID ticketId, String agentName) {
        log.info("Assignation ticket ID {} à l'agent : {}", ticketId, agentName);
        
        SupportTicket ticket = getTicketById(ticketId);
        
        // Validation des règles métier
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalStateException("Impossible d'assigner un ticket résolu ou fermé");
        }
        
        ticket.setAssignedAgent(agentName);
        
        // Changer le statut vers IN_PROGRESS si le ticket était OPEN
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        
        SupportTicket updatedTicket = ticketRepository.save(ticket);
        log.info("Ticket assigné à l'agent {} : {} (ID: {})", agentName, updatedTicket.getSubject(), updatedTicket.getId());
        
        return updatedTicket;
    }
    
    /**
     * Résoudre un ticket
     */
    public SupportTicket resolveTicket(UUID ticketId, String resolution) {
        log.info("Résolution ticket ID : {}", ticketId);
        
        SupportTicket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalStateException("Impossible de résoudre un ticket fermé");
        }
        
        ticket.setStatus(TicketStatus.RESOLVED);
        
        // Ajouter la résolution dans la description si fournie
        if (resolution != null && !resolution.trim().isEmpty()) {
            String updatedDescription = ticket.getDescription() + "\n\n[RÉSOLUTION] " + resolution;
            ticket.setDescription(updatedDescription);
        }
        
        SupportTicket resolvedTicket = ticketRepository.save(ticket);
        log.info("Ticket résolu : {} (ID: {})", resolvedTicket.getSubject(), resolvedTicket.getId());
        
        return resolvedTicket;
    }
    
    /**
     * Fermer un ticket
     */
    public SupportTicket closeTicket(UUID ticketId) {
        log.info("Fermeture ticket ID : {}", ticketId);
        
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        
        SupportTicket closedTicket = ticketRepository.save(ticket);
        log.info("Ticket fermé : {} (ID: {})", closedTicket.getSubject(), closedTicket.getId());
        
        return closedTicket;
    }
    
    /**
     * Rouvrir un ticket
     */
    public SupportTicket reopenTicket(UUID ticketId) {
        log.info("Réouverture ticket ID : {}", ticketId);
        
        SupportTicket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.IN_PROGRESS) {
            throw new IllegalStateException("Le ticket est déjà ouvert ou en cours de traitement");
        }
        
        ticket.setStatus(TicketStatus.OPEN);
        
        SupportTicket reopenedTicket = ticketRepository.save(ticket);
        log.info("Ticket rouvert : {} (ID: {})", reopenedTicket.getSubject(), reopenedTicket.getId());
        
        return reopenedTicket;
    }
    
    /**
     * Récupérer les tickets actifs (OPEN + IN_PROGRESS)
     */
    @Transactional(readOnly = true)
    public List<SupportTicket> getActiveTickets() {
        log.debug("Récupération des tickets actifs");
        List<SupportTicket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
        List<SupportTicket> inProgressTickets = ticketRepository.findByStatus(TicketStatus.IN_PROGRESS);
        
        openTickets.addAll(inProgressTickets);
        return openTickets;
    }
    
    /**
     * Compter les tickets par statut pour un utilisateur
     */
    @Transactional(readOnly = true)
    public long countTicketsByUserAndStatus(UUID userId, TicketStatus status) {
        return ticketRepository.findByUserIdAndStatus(userId, status).size();
    }
    
    /**
     * Auto-assignation d'agent disponible
     */
    private void assignAvailableAgent(SupportTicket ticket) {
        log.debug("Tentative d'auto-assignation d'agent pour le ticket : {}", ticket.getSubject());
        
        // Récupérer tous les agents
        List<User> agents = userService.getAllAgents();
        
        if (agents.isEmpty()) {
            log.warn("Aucun agent disponible pour l'auto-assignation");
            return;
        }
        
        // Logique d'assignation basique : trouver l'agent avec le moins de tickets actifs
        String bestAgent = null;
        int minActiveTickets = Integer.MAX_VALUE;
        
        for (User agent : agents) {
            String agentName = agent.getFirstName() + " " + agent.getLastName();
            List<SupportTicket> agentTickets = ticketRepository.findByAssignedAgent(agentName);
            
            // Compter seulement les tickets actifs
            long activeTickets = agentTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.IN_PROGRESS)
                    .count();
            
            if (activeTickets < minActiveTickets) {
                minActiveTickets = (int) activeTickets;
                bestAgent = agentName;
            }
        }
        
        if (bestAgent != null && minActiveTickets < 5) { // Seuil configurable
            ticket.setAssignedAgent(bestAgent);
            log.info("Ticket auto-assigné à l'agent : {}", bestAgent);
        } else {
            log.info("Tous les agents ont trop de tickets actifs, ticket non assigné automatiquement");
        }
    }
    
    /**
     * Validation métier des données de ticket
     */
    private void validateTicket(SupportTicket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Le ticket ne peut pas être null");
        }
        
        if (ticket.getUserId() == null) {
            throw new IllegalArgumentException("L'ID utilisateur est obligatoire");
        }
        
        if (ticket.getSubject() == null || ticket.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Le sujet du ticket est obligatoire");
        }
        
        if (ticket.getSubject().length() > 255) {
            throw new IllegalArgumentException("Le sujet ne peut pas dépasser 255 caractères");
        }
        
        if (ticket.getDescription() != null && ticket.getDescription().length() > 5000) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 5000 caractères");
        }
        
        log.debug("Validation ticket réussie pour : {}", ticket.getSubject());
    }
    
    /**
     * Valider les transitions de statut selon les règles métier
     */
    private void validateStatusTransition(TicketStatus currentStatus, TicketStatus newStatus) {
        if (newStatus == null || currentStatus == newStatus) {
            return; // Pas de changement
        }
        
        log.debug("Validation transition statut : {} -> {}", currentStatus, newStatus);
        
        // Règles de transition autorisées
        switch (currentStatus) {
            case OPEN:
                if (newStatus != TicketStatus.IN_PROGRESS && newStatus != TicketStatus.CLOSED) {
                    throw new IllegalStateException("Transition invalide: OPEN peut seulement aller vers IN_PROGRESS ou CLOSED");
                }
                break;
                
            case IN_PROGRESS:
                if (newStatus != TicketStatus.RESOLVED && newStatus != TicketStatus.CLOSED && newStatus != TicketStatus.OPEN) {
                    throw new IllegalStateException("Transition invalide: IN_PROGRESS peut seulement aller vers RESOLVED, CLOSED ou OPEN");
                }
                break;
                
            case RESOLVED:
                if (newStatus != TicketStatus.CLOSED && newStatus != TicketStatus.OPEN) {
                    throw new IllegalStateException("Transition invalide: RESOLVED peut seulement aller vers CLOSED ou OPEN");
                }
                break;
                
            case CLOSED:
                if (newStatus != TicketStatus.OPEN) {
                    throw new IllegalStateException("Transition invalide: CLOSED peut seulement aller vers OPEN");
                }
                break;
        }
        
        log.debug("Transition de statut validée : {} -> {}", currentStatus, newStatus);
    }
}