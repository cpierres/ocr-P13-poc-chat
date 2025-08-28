package com.cpierres.p13.poc.backend.controller;

import com.cpierres.p13.poc.backend.dto.MockUserInfo;
import com.cpierres.p13.poc.backend.service.mock.MockAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur POC pour simuler l'authentification
 */
@RestController
@RequestMapping("/api/mock")
@CrossOrigin(origins = "*")
@Slf4j
public class MockAuthController {

    private final MockAuthService mockAuthService;

    public MockAuthController(MockAuthService mockAuthService) {
        this.mockAuthService = mockAuthService;
    }

    /**
     * Endpoint POC pour "se connecter" en tant que client ou agent
     */
    @PostMapping("/login/{userType}")
    public ResponseEntity<Map<String, Object>> mockLogin(@PathVariable String userType) {
        try {
            log.info("🎭 Connexion POC en tant que : {}", userType);

            String token = mockAuthService.generateToken(userType);
            var tokenInfo = mockAuthService.validateToken(token);

            Map<String, Object> response = Map.of(
                    "token", token,
                    "userId", tokenInfo.getUserId(),
                    "email", tokenInfo.getEmail(),
                    "role", tokenInfo.getRole(),
                    "message", "Connexion POC réussie en tant que " + userType
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur connexion POC : {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lister les utilisateurs disponibles pour le POC
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, MockUserInfo>> getAvailableUsers() {
        return ResponseEntity.ok(mockAuthService.getAllMockUsers());
    }
}
