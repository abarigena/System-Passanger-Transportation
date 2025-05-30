package com.abarigena.userservice.service;

import com.abarigena.dto.kafka.UserRegisteredEvent;
import com.abarigena.userservice.dto.ConfirmPhoneDto;
import com.abarigena.userservice.dto.PublicUserDto;
import com.abarigena.userservice.dto.UserProfileDto;
import com.abarigena.userservice.dto.UserUpdateDto;
import com.abarigena.dto.kafka.UserRatingUpdatedEvent;

import com.abarigena.userservice.entity.UserProfile;
import com.abarigena.userservice.exception.BadRequestException;
import com.abarigena.userservice.kafka.KafkaProducerService;
import com.abarigena.userservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final KafkaProducerService kafkaProducerService;



    @Override
    public void createUserProfile(UserRegisteredEvent event) {
        if (userProfileRepository.existsById(event.getUserId())) {
            log.warn("User profile already exists for userId: {}", event.getUserId());
            return;
        }
        UserProfile profile = new UserProfile();
        profile.setUserId(event.getUserId());
        profile.setEmail(event.getEmail());
        profile.setFirstName(event.getFirstName());
        profile.setLastName(event.getLastName());
        profile.setPhoneNumber(event.getPhoneNumber());

        userProfileRepository.save(profile);
        log.info("User profile created for userId: {}", event.getUserId());
    }

    @Transactional(readOnly = true)
    @Override
    public UserProfileDto getCurrentUserProfile(UUID userId) {
        return getUserProfileDtoById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public PublicUserDto getPublicUserProfile(UUID userId) {
        UserProfile profile = findUserProfileById(userId);
        // Маппинг в PublicUserDto
        return PublicUserDto.builder()
                .id(profile.getUserId())
                .firstName(profile.getFirstName())
                .photoUrl(profile.getPhotoUrl())
                .averageRating(profile.getAverageRating())
                .build();
    }

    @Override
    public UserProfileDto updateCurrentUserProfile(UUID userId, UserUpdateDto userUpdateDto) {
        // Ищем профиль по переданному ID
        UserProfile profile = findUserProfileById(userId);
        Map<String, Object> updatedFields = new HashMap<>();

        // Логика обновления остается прежней...
        if (userUpdateDto.getFirstName() != null && !Objects.equals(profile.getFirstName(), userUpdateDto.getFirstName())) {
            profile.setFirstName(userUpdateDto.getFirstName());
            updatedFields.put("firstName", profile.getFirstName());
        }
        // ... (обновление lastName, phoneNumber, photoUrl, additionalInfo) ...
        if (userUpdateDto.getPhoneNumber() != null && !Objects.equals(profile.getPhoneNumber(), userUpdateDto.getPhoneNumber())) {
            profile.setPhoneNumber(userUpdateDto.getPhoneNumber());
            profile.setPhoneVerified(false);
            updatedFields.put("phoneNumber", profile.getPhoneNumber());
            updatedFields.put("phoneVerified", false);
        }
        if (userUpdateDto.getPhotoUrl() != null && !Objects.equals(profile.getPhotoUrl(), userUpdateDto.getPhotoUrl())) {
            profile.setPhotoUrl(userUpdateDto.getPhotoUrl());
            updatedFields.put("photoUrl", profile.getPhotoUrl());
        }
        if (userUpdateDto.getAdditionalInfo() != null && !Objects.equals(profile.getAdditionalInfo(), userUpdateDto.getAdditionalInfo())) {
            profile.setAdditionalInfo(userUpdateDto.getAdditionalInfo());
            updatedFields.put("additionalInfo", profile.getAdditionalInfo());
        }


        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("User profile updated for userId: {}", userId);

        if (!updatedFields.isEmpty()) {
            kafkaProducerService.sendUserProfileUpdatedEvent(userId, updatedFields);
        }

        return mapToUserProfileDto(updatedProfile);
    }

    @Override
    public void initiatePhoneVerification(UUID userId) {
        UserProfile profile = findUserProfileById(userId);

        if (profile.getPhoneNumber() == null || profile.getPhoneNumber().isBlank()) {
            throw new BadRequestException("Phone number is not set for the user.");
        }
        if (Boolean.TRUE.equals(profile.getPhoneVerified())) {
            throw new BadRequestException("Phone number is already verified.");
        }

        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));
        log.info("Generated phone verification code {} for user {}", verificationCode, userId);
        log.warn("Phone verification code storage is not implemented. Code: {}", verificationCode);
        log.info("Simulating sending SMS verification code to {}", profile.getPhoneNumber());
    }

    @Override
    public void confirmPhoneVerification(UUID userId, ConfirmPhoneDto confirmPhoneDto) {
        UserProfile profile = findUserProfileById(userId);
        String providedCode = confirmPhoneDto.getCode();

        if (Boolean.TRUE.equals(profile.getPhoneVerified())) {
            throw new BadRequestException("Phone number is already verified.");
        }

        String storedCode = "123456";
        log.warn("Phone verification code retrieval is not implemented. Using mock code: {}", storedCode);

        if (storedCode == null || !storedCode.equals(providedCode)) {
            throw new BadRequestException("Invalid or expired verification code.");
        }

        profile.setPhoneVerified(true);
        userProfileRepository.save(profile);
        log.info("Phone number verified for user: {}", userId);

        kafkaProducerService.sendUserContactVerifiedEvent(userId, "phone");
    }


    // Вызывается Kafka консьюмером
    @Override
    public void updateUserRating(UserRatingUpdatedEvent event) {
        UserProfile profile = findUserProfileById(event.getUserId());
        profile.setAverageRating(event.getNewAverageRating());
        userProfileRepository.save(profile);
        log.info("Updated average rating for user {}: {}", event.getUserId(), event.getNewAverageRating());
    }


    // --- Вспомогательные методы ---
    private UserProfile findUserProfileById(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with id: " + userId));
    }

    private UserProfileDto getUserProfileDtoById(UUID userId) {
        UserProfile profile = findUserProfileById(userId);
        return mapToUserProfileDto(profile);
    }


    private UserProfileDto mapToUserProfileDto(UserProfile profile) {
        // Конвертируем время создания в LocalDateTime (если оно Instant)
        LocalDateTime registrationDateTime = (profile.getCreatedAt() != null)
                ? profile.getCreatedAt()
                : null;

        return UserProfileDto.builder()
                .id(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .phoneVerified(profile.getPhoneVerified())
                .photoUrl(profile.getPhotoUrl())
                .additionalInfo(profile.getAdditionalInfo())
                .registrationDate(registrationDateTime)
                .averageRating(profile.getAverageRating())
                .build();
    }
}