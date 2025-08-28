package com.cpierres.p13.poc.backend.service.mock;

import com.cpierres.p13.poc.backend.dto.MockUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class MockUserService {

    private final MockAuthService mockAuthService;

    public MockUserService(MockAuthService mockAuthService) {
        this.mockAuthService = mockAuthService;
    }

    /**
     * Simule l'appel vers le User Service pour récupérer le profil
     */
    public MockUserInfo getUserProfile(UUID userId) {
        // Dans un vrai contexte : appel REST vers user-service
        // Pour le POC : simulation locale

        MockUserInfo user = mockAuthService.getAllMockUsers().values()
                .stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);

        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé : " + userId);
        }

        log.debug("Profil utilisateur récupéré : {} {}", user.getFirstName(), user.getLastName());
        return user;
    }

    /**
     * Simule l'appel vers le User Service par email
     */
    public MockUserInfo getUserByEmail(String email) {
        MockUserInfo user = mockAuthService.getAllMockUsers().values()
                .stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé par email : " + email);
        }

        return user;
    }
}
