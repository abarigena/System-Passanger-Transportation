package com.abarigena.authenticationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value(value = "auth-events")
    private String userRegisteredTopic;

    @Value(value = "auth-events")
    private String userEmailVerifiedTopic;

    @Value(value = "auth-events")
    private String userPasswordChangedTopic;

    public void sendUserRegisteredEvent(UUID userId, String email, String firstName, String lastName, String phoneNumber) {
        UserRegisteredEventDto event = UserRegisteredEventDto.builder()
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .timestamp(Instant.now())
                .build();
        try {
            log.info("Sending UserRegisteredEvent for userId: {}", userId);
            kafkaTemplate.send(userRegisteredTopic, userId.toString(), event); // Используем userId как ключ Kafka сообщения
        } catch (Exception e) {
            log.error("Error sending UserRegisteredEvent for userId {}: {}", userId, e.getMessage(), e);
            // Здесь можно добавить логику повторной отправки или сохранения события для последующей отправки
        }
    }

    public void sendUserEmailVerifiedEvent(UUID userId) {
        UserEmailVerifiedEventDto event = UserEmailVerifiedEventDto.builder()
                .userId(userId)
                .timestamp(Instant.now())
                .build();
        try {
            log.info("Sending UserEmailVerifiedEvent for userId: {}", userId);
            kafkaTemplate.send(userEmailVerifiedTopic, userId.toString(), event);
        } catch (Exception e) {
            log.error("Error sending UserEmailVerifiedEvent for userId {}: {}", userId, e.getMessage(), e);
        }
    }

    public void sendUserPasswordChangedEvent(UUID userId) {
        UserPasswordChangedEventDto event = UserPasswordChangedEventDto.builder()
                .userId(userId)
                .timestamp(Instant.now())
                .build();
        try {
            log.info("Sending UserPasswordChangedEvent for userId: {}", userId);
            kafkaTemplate.send(userPasswordChangedTopic, userId.toString(), event);
        } catch (Exception e) {
            log.error("Error sending UserPasswordChangedEvent for userId {}: {}", userId, e.getMessage(), e);
        }
    }

}