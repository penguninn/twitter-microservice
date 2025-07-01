package com.david.notification_service.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FcmConfig {

    @Value("${app.firebase.config-file}")
    private String firebaseConfigFile;

    @PostConstruct
    public void init() {
        log.info("FcmConfig::init - Execution started");
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream inputStream = classLoader.getResourceAsStream(firebaseConfigFile)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found: " + firebaseConfigFile);
                }
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                } else {
                    log.info("FcmConfig::init - Firebase app already initialized, skipping initialization");
                }
                log.info("FcmConfig::init - Firebase initialized successfully");
            }
        } catch (Exception e) {
            log.error("FcmConfig::init - Error initializing Firebase", e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
