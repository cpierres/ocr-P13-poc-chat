package com.cpierres.p13.poc.backend.service;

import com.cpierres.p13.poc.backend.entity.User;
import com.cpierres.p13.poc.backend.entity.UserRole;
import com.cpierres.p13.poc.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Créer un nouvel utilisateur avec validation métier
     */
    public User createUser(User user) {
        log.info("Création d'un utilisateur : {}", user.getEmail());
        
        // Validation métier
        validateUser(user);
        
        // Vérification unicité email
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Tentative de création d'utilisateur avec email existant : {}", user.getEmail());
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }
        
        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès : {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Récupérer tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Récupération de tous les utilisateurs");
        return userRepository.findAll();
    }
    
    /**
     * Récupérer un utilisateur par ID
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        log.debug("Récupération utilisateur par ID : {}", id);
        
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé avec l'ID : {}", id);
                    return new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + id);
                });
    }
    
    /**
     * Récupérer un utilisateur par email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.debug("Récupération utilisateur par email : {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Mettre à jour un utilisateur
     */
    public User updateUser(UUID id, User userDetails) {
        log.info("Mise à jour utilisateur ID : {}", id);
        
        User existingUser = getUserById(id);
        
        // Vérification email unique (si changé)
        if (!existingUser.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
            }
        }
        
        // Mise à jour des champs
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        
        if (userDetails.getRole() != null) {
            existingUser.setRole(userDetails.getRole());
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("Utilisateur mis à jour : {} (ID: {})", updatedUser.getEmail(), updatedUser.getId());
        
        return updatedUser;
    }
    
    /**
     * Supprimer un utilisateur
     */
    public void deleteUser(UUID id) {
        log.info("Suppression utilisateur ID : {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + id);
        }
        
        userRepository.deleteById(id);
        log.info("Utilisateur supprimé avec succès : {}", id);
    }
    
    /**
     * Assigner le rôle agent à un utilisateur
     */
    public User assignAgentRole(UUID userId) {
        log.info("Attribution du rôle AGENT à l'utilisateur ID : {}", userId);
        
        User user = getUserById(userId);
        user.setRole(UserRole.AGENT);
        
        User updatedUser = userRepository.save(user);
        log.info("Rôle AGENT assigné à l'utilisateur : {} (ID: {})", updatedUser.getEmail(), updatedUser.getId());
        
        return updatedUser;
    }
    
    /**
     * Récupérer tous les agents
     */
    @Transactional(readOnly = true)
    public List<User> getAllAgents() {
        log.debug("Récupération de tous les agents");
        // Note: Cette méthode nécessitera l'ajout d'une requête au repository
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.AGENT)
                .toList();
    }
    
    /**
     * Récupérer tous les clients
     */
    @Transactional(readOnly = true)
    public List<User> getAllClients() {
        log.debug("Récupération de tous les clients");
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.CLIENT)
                .toList();
    }
    
    /**
     * Vérifier si un utilisateur existe
     */
    @Transactional(readOnly = true)
    public boolean userExists(UUID id) {
        return userRepository.existsById(id);
    }
    
    /**
     * Vérifier si un email existe
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Validation métier des données utilisateur
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null");
        }
        
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }
        
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        
        // Validation format email basique
        if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new IllegalArgumentException("Format d'email invalide");
        }
        
        // Validation longueur des champs
        if (user.getFirstName().length() > 100) {
            throw new IllegalArgumentException("Le prénom ne peut pas dépasser 100 caractères");
        }
        
        if (user.getLastName().length() > 100) {
            throw new IllegalArgumentException("Le nom ne peut pas dépasser 100 caractères");
        }
        
        if (user.getEmail().length() > 255) {
            throw new IllegalArgumentException("L'email ne peut pas dépasser 255 caractères");
        }
        
        log.debug("Validation utilisateur réussie pour : {}", user.getEmail());
    }
}