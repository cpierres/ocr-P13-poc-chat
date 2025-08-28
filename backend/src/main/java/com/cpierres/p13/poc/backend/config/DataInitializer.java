package com.cpierres.p13.poc.backend.config;

import com.cpierres.p13.poc.backend.entity.User;
import com.cpierres.p13.poc.backend.entity.UserRole;
import com.cpierres.p13.poc.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            log.info("🚀 Initialisation des données de démonstration POC Chat...");

            // Vérifier la connexion à la base de données
            long userCount = userRepository.count();
            log.info("📊 Nombre d'utilisateurs existants : {}", userCount);

            initializeUsers();
            log.info("✅ Données de démonstration initialisées avec succès !");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation des données : {}", e.getMessage(), e);
            throw e; // Re-lancer l'exception pour arrêter l'application en cas d'erreur critique
        }
    }

    private void initializeUsers() {
        try {
            // Créer un client de démonstration
            if (!userRepository.existsByEmail("client.demo@yourcaryourway.com")) {
                User client = new User();
                client.setFirstName("Christophe");
                client.setLastName("Pierrès");
                client.setEmail("client.demo@yourcaryourway.com");
                client.setRole(UserRole.CLIENT);
                userRepository.save(client);
                log.info("✅ Client de démonstration créé : {} (ID: {})", client.getEmail(), client.getId());
            } else {
                log.info("ℹ️ Client de démonstration existe déjà : client.demo@yourcaryourway.com");
            }

            // Créer un agent de démonstration
            if (!userRepository.existsByEmail("agent.demo@yourcaryourway.com")) {
                User agent = new User();
                agent.setFirstName("Marie");
                agent.setLastName("Agent");
                agent.setEmail("agent.demo@yourcaryourway.com");
                agent.setRole(UserRole.AGENT);
                userRepository.save(agent);
                log.info("✅ Agent de démonstration créé : {} (ID: {})", agent.getEmail(), agent.getId());
            } else {
                log.info("ℹ️ Agent de démonstration existe déjà : agent.demo@yourcaryourway.com");
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création des utilisateurs de démonstration : {}", e.getMessage(), e);
            throw e;
        }
    }
}