package com.badmintonshop.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK Configuration.
 * <p>
 * This class initializes the Firebase Admin SDK on application startup.
 * It reads the service account credentials from the classpath and configures
 * the Firebase application with the target storage bucket.
 * </p>
 * <p>
 * Required properties in {@code application.properties} (or {@code .env}):
 * <ul>
 * <li>{@code firebase.config.path} - Classpath path to the service account JSON file.</li>
 * <li>{@code firebase.storage.bucket} - Firebase Storage bucket name (e.g., {@code my-app.appspot.com}).</li>
 * </ul>
 * </p>
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @Value("${firebase.storage.bucket}")
    private String firebaseBucket;

    /**
     * Initializes the Firebase Admin SDK after the bean is constructed.
     * <p>
     * This method runs once on startup. If a {@link FirebaseApp} instance already
     * exists (e.g., in a hot-reload environment), initialization is skipped to
     * prevent duplicate app registration errors.
     * </p>
     *
     * @throws IOException If the service account file cannot be found or read.
     */
    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(firebaseBucket)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully. Bucket: {}", firebaseBucket);
            } else {
                log.info("Firebase already initialized. Skipping re-initialization.");
            }

        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK. Config path: '{}'. Reason: {}",
                    firebaseConfigPath, e.getMessage());
        }
    }
}
