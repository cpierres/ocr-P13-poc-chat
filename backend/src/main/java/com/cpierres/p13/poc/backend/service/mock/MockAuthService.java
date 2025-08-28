package com.cpierres.p13.poc.backend.service.mock;

import com.cpierres.p13.poc.backend.dto.AuthTokenInfo;
import com.cpierres.p13.poc.backend.dto.MockUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MockAuthService {

    // Simulation des utilisateurs de démonstration
    private final Map<String, MockUserInfo> mockUsers = new ConcurrentHashMap<>();

    public MockAuthService() {
        initializeMockUsers();
    }

    private void initializeMockUsers() {
        // Client de démonstration
        MockUserInfo client = new MockUserInfo(
                UUID.randomUUID(),
                "christophe.pierres@hotmail.com",
                "Christophe",
                "Pierrès",
                "CLIENT"
        );

        // Agent de démonstration
        MockUserInfo agent = new MockUserInfo(
                UUID.randomUUID(),
                "marie.agent@yourcaryourway.com",
                "Marie",
                "Agent",
                "AGENT"
        );

        mockUsers.put("mock-client-token", client);
        mockUsers.put("mock-agent-token", agent);

        log.info("🎭 Mock Auth Service initialisé avec {} utilisateurs", mockUsers.size());
    }

    /**
     * Simule la validation d'un token JWT
     */
    public AuthTokenInfo validateToken(String token) {
        MockUserInfo user = mockUsers.get(token);
        if (user == null) {
            throw new IllegalArgumentException("Token invalide : " + token);
        }

        log.debug("Token validé pour utilisateur : {} ({})", user.getEmail(), user.getRole());
        return new AuthTokenInfo(user.getId(), user.getEmail(), user.getRole(), true);
    }

    /**
     * Simule la génération d'un token pour un utilisateur
     */
    public String generateToken(String userType) {
        switch (userType.toUpperCase()) {
            case "CLIENT":
                return "mock-client-token";
            case "AGENT":
                return "mock-agent-token";
            default:
                throw new IllegalArgumentException("Type d'utilisateur inconnu : " + userType);
        }
    }

    public Map<String, MockUserInfo> getAllMockUsers() {
        return Map.copyOf(mockUsers);
    }
}