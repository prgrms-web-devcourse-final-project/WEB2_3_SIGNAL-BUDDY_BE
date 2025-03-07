package org.programmers.signalbuddyfinal.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FcmConfig {

    @Value("${fcm.secret-path}")
    private String secretKeyPath;

    @Value("${fcm.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 이미 FirebaseApp이 초기화되었는지 확인
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (!firebaseApps.isEmpty()) {
            return firebaseApps.get(0); // 기존 앱 반환
        }

        ClassPathResource resource = new ClassPathResource(secretKeyPath);
        InputStream secretKey = resource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(secretKey))
            .setProjectId(projectId)
            .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        return FirebaseMessaging.getInstance(firebaseApp());
    }
}
