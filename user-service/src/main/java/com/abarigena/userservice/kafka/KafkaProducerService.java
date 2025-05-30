package com.abarigena.userservice.kafka;


import com.abarigena.dto.kafka.UserContactVerifiedEvent;
import com.abarigena.dto.kafka.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user-profile-updated}")
    private String userProfileUpdatedTopic;

    @Value("${spring.kafka.topic.user-contact-verified}")
    private String userContactVerifiedTopic;

    public void sendUserProfileUpdatedEvent(UUID userId, Map<String, Object> updatedFields) {
        UserProfileUpdatedEvent event = UserProfileUpdatedEvent.builder()
                .userId(userId)
                .updatedFields(updatedFields)
                .timestamp(Instant.now())
                .build();
        try {
            log.info("Sending UserProfileUpdatedEvent for userId: {}", userId);
            kafkaTemplate.send(userProfileUpdatedTopic, userId.toString(), event);
        } catch (Exception e) {
            log.error("Error sending UserProfileUpdatedEvent for userId {}: {}", userId, e.getMessage(), e);
        }
    }

    public void sendUserContactVerifiedEvent(UUID userId, String contactType) {
        UserContactVerifiedEvent event = UserContactVerifiedEvent.builder()
                .userId(userId)
                .contactType(contactType)
                .timestamp(Instant.now())
                .build();
        try {
            log.info("Sending UserContactVerifiedEvent for userId: {}, type: {}", userId, contactType);
            kafkaTemplate.send(userContactVerifiedTopic, userId.toString(), event);
        } catch (Exception e) {
            log.error("Error sending UserContactVerifiedEvent for userId {}: {}", userId, e.getMessage(), e);
        }
    }
}