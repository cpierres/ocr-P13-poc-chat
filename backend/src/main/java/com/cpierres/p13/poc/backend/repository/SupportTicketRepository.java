package com.cpierres.p13.poc.backend.repository;

import com.cpierres.p13.poc.backend.entity.SupportTicket;
import com.cpierres.p13.poc.backend.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    
    List<SupportTicket> findByUserId(UUID userId);
    
    List<SupportTicket> findByAssignedAgent(String assignedAgent);
    
    List<SupportTicket> findByStatus(TicketStatus status);
    
    List<SupportTicket> findByUserIdAndStatus(UUID userId, TicketStatus status);
}