package com.abarigena.userservice.kafka;

import com.abarigena.dto.kafka.UserRegisteredEvent;
import com.abarigena.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final UserService userService; // Инжектируем основной сервис

    // Слушаем топик, указанный в application.yml для user-registered
    @KafkaListener(topics = "${spring.kafka.topic.user-registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listenUserRegistered(@Payload UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for userId: {}", event.getUserId());
        try {
            userService.createUserProfile(event);
        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent for userId {}: {}", event.getUserId(), e.getMessage(), e);

        }
    }

//
//    @KafkaListener(topics = "${app.kafka.topic-user-rating-updated}",
//            groupId = "${spring.kafka.consumer.group-id}",
//            containerFactory = "kafkaListenerContainerFactory")
//    public void listenUserRatingUpdated(@Payload UserRatingUpdatedEvent event) {
//        log.info("Received UserRatingUpdatedEvent for userId: {}", event.getUserId());
//        try {
//            userService.updateUserRating(event);
//        } catch (Exception e) {
//            log.error("Error processing UserRatingUpdatedEvent for userId {}: {}", event.getUserId(), e.getMessage(), e);
//
//        }
//    }

     /*

    @KafkaListener(topics = "${app.kafka.topic-user-email-verified}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void listenUserEmailVerified(@Payload UserEmailVerifiedEvent event) {
        log.info("Received UserEmailVerifiedEvent for userId: {}", event.getUserId());
        try {
            // userService.handleEmailVerified(event);
        } catch (Exception e) {
            log.error("Error processing UserEmailVerifiedEvent for userId {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }
    */

}