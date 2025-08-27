package com.cpierres.p13.poc.backend.config;

import com.cpierres.p13.poc.backend.entity.User;
import com.cpierres.p13.poc.backend.entity.UserRole;
import com.cpierres.p13.poc.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Initialisation des données de démonstration POC Chat...");
        initializeUsers();
        log.info("✅ Données de démonstration initialisées avec succès !");
    }
    
    private void initializeUsers() {
        // Créer un client de démonstration
        if (!userRepository.existsByEmail("client.demo@yourcaryourway.com")) {
            User client = new User();
            client.setFirstName("Christophe");
            client.setLastName("Pierrès");
            client.setEmail("cpierres@hotmail.com");
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
    }
}