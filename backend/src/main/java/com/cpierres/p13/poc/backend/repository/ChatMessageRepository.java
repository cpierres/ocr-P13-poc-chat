package com.cpierres.p13.poc.backend.repository;

import com.cpierres.p13.poc.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    
    List<ChatMessage> findByTicketId(UUID ticketId);
    
    List<ChatMessage> findByTicketIdOrderByTimestampAsc(UUID ticketId);
    
    List<ChatMessage> findBySenderId(UUID senderId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.ticketId = :ticketId AND m.timestamp >= :since ORDER BY m.timestamp ASC")
    List<ChatMessage> findByTicketIdAndTimestampAfter(UUID ticketId, LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.ticketId = :ticketId")
    Long countByTicketId(UUID ticketId);
}