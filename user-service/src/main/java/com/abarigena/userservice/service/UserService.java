package com.abarigena.userservice.service;



import com.abarigena.dto.kafka.UserRatingUpdatedEvent;
import com.abarigena.dto.kafka.UserRegisteredEvent;
import com.abarigena.userservice.dto.ConfirmPhoneDto;
import com.abarigena.userservice.dto.PublicUserDto;
import com.abarigena.userservice.dto.UserProfileDto;
import com.abarigena.userservice.dto.UserUpdateDto;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserService {


    void createUserProfile(UserRegisteredEvent event);

    @Transactional(readOnly = true)
    UserProfileDto getCurrentUserProfile(UUID userId);

    @Transactional(readOnly = true)
    PublicUserDto getPublicUserProfile(UUID userId);

    UserProfileDto updateCurrentUserProfile(UUID userId, UserUpdateDto userUpdateDto);

    void initiatePhoneVerification(UUID userId);

    void confirmPhoneVerification(UUID userId, ConfirmPhoneDto confirmPhoneDto);

    void updateUserRating(UserRatingUpdatedEvent event);
}
