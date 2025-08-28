package com.cpierres.p13.poc.backend.service;

import com.cpierres.p13.poc.backend.dto.AuthTokenInfo;
import com.cpierres.p13.poc.backend.dto.MockUserInfo;
import com.cpierres.p13.poc.backend.service.mock.MockAuthService;
import com.cpierres.p13.poc.backend.service.mock.MockUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service abstrait pour la gestion des utilisateurs dans le contexte Support
 * Prépare l'intégration avec les vrais services auth/user
 */
@Service
@Slf4j
public class SupportUserService {

    private final MockAuthService mockAuthService;
    private final MockUserService mockUserService;

    public SupportUserService(MockAuthService mockAuthService, MockUserService mockUserService) {
        this.mockAuthService = mockAuthService;
        this.mockUserService = mockUserService;
    }

    /**
     * Résoudre un utilisateur à partir d'un token d'authentification
     * Dans la version finale : validation JWT + appel user-service
     */
    public MockUserInfo resolveUserFromToken(String authToken) {
        try {
            // Validation du token (simulation auth-service)
            AuthTokenInfo tokenInfo = mockAuthService.validateToken(authToken);

            // Récupération du profil utilisateur (simulation user-service)
            return mockUserService.getUserProfile(tokenInfo.getUserId());

        } catch (Exception e) {
            log.error("Erreur lors de la résolution utilisateur : {}", e.getMessage());
            throw new IllegalArgumentException("Token utilisateur invalide");
        }
    }

    /**
     * Obtenir les informations utilisateur pour le chat
     */
    public MockUserInfo getChatUserInfo(UUID userId) {
        return mockUserService.getUserProfile(userId);
    }
}