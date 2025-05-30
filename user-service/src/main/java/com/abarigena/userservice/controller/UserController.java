package com.abarigena.userservice.controller;

import com.abarigena.userservice.dto.ConfirmPhoneDto;
import com.abarigena.userservice.dto.PublicUserDto;
import com.abarigena.userservice.dto.UserProfileDto;
import com.abarigena.userservice.dto.UserUpdateDto;
import com.abarigena.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    // Получение полного профиля пользователя по ID (замена /me)
    @GetMapping("/{id}/profile") // Новый путь вместо /me
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable UUID id) {
        log.warn("SECURITY WARNING: Accessing user profile {} without authentication", id); // Предупреждение
        UserProfileDto userProfile = userService.getCurrentUserProfile(id); // Вызываем метод с ID
        return ResponseEntity.ok(userProfile);
    }

    // Обновление профиля пользователя по ID (замена /me)
    @PutMapping("/{id}") // Новый путь вместо /me
    public ResponseEntity<UserProfileDto> updateUserProfile(@PathVariable UUID id,
                                                            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.warn("SECURITY WARNING: Updating user profile {} without authentication", id); // Предупреждение
        UserProfileDto updatedProfile = userService.updateCurrentUserProfile(id, userUpdateDto); // Вызываем метод с ID
        return ResponseEntity.ok(updatedProfile);
    }

    // Инициировать верификацию телефона по ID (замена /me/...)
    @PostMapping("/{id}/initiate-phone-verification") // Новый путь
    public ResponseEntity<?> initiatePhoneVerification(@PathVariable UUID id) {
        log.warn("SECURITY WARNING: Initiating phone verification for user {} without authentication", id); // Предупреждение
        userService.initiatePhoneVerification(id); // Вызываем метод с ID
        return ResponseEntity.ok(new ApiResponse(true, "Verification code sent to your phone number."));
    }

    // Подтвердить верификацию телефона по ID (замена /me/...)
    @PostMapping("/{id}/confirm-phone-verification") // Новый путь
    public ResponseEntity<?> confirmPhoneVerification(@PathVariable UUID id,
                                                      @Valid @RequestBody ConfirmPhoneDto confirmPhoneDto) {
        log.warn("SECURITY WARNING: Confirming phone verification for user {} without authentication", id); // Предупреждение
        userService.confirmPhoneVerification(id, confirmPhoneDto); // Вызываем метод с ID
        return ResponseEntity.ok(new ApiResponse(true,"Phone number verified successfully."));
    }


    // --- Публичный эндпоинт остается без изменений ---
    @GetMapping("/{id}")
    public ResponseEntity<PublicUserDto> getPublicUserProfile(@PathVariable UUID id) {
        PublicUserDto publicProfile = userService.getPublicUserProfile(id);
        return ResponseEntity.ok(publicProfile);
    }

    // Вспомогательный класс для простых ответов
    @Data
    @AllArgsConstructor
    static class ApiResponse {
        private boolean success;
        private String message;
    }
}