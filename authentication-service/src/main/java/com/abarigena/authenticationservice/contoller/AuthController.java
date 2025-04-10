package com.abarigena.authenticationservice.contoller;


import com.abarigena.authenticationservice.dto.*;
import com.abarigena.authenticationservice.exception.InvalidTokenException;
import com.abarigena.authenticationservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        // Ошибки валидации и ConflictException будут обработаны GlobalExceptionHandler
        authService.register(registerDto);
        // В ТЗ: 201 Created / 200 ОК с сообщением о подтверждении email
        // Возвращаем 201 для RESTful стиля
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true,"User registered successfully. Please check your email to verify your account."));
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) { // Прием токена как параметра запроса
        // Или через DTO: public ResponseEntity<?> confirmEmail(@Valid @RequestBody ConfirmDto confirmDto) { authService.confirmEmail(confirmDto.getToken()); }
        authService.confirmEmail(token);
        return ResponseEntity.ok(new ApiResponse(true, "Email confirmed successfully. You can now login."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        // AuthenticationException будет обработана GlobalExceptionHandler -> 401
        TokenDto tokenDto = authService.login(loginDto);
        return ResponseEntity.ok(tokenDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refreshToken(@Valid @RequestBody RefreshDto refreshDto) {
        // InvalidTokenException будет обработана GlobalExceptionHandler
        TokenDto tokenDto = authService.refreshToken(refreshDto);
        return ResponseEntity.ok(tokenDto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        // ResourceNotFoundException будет обработана GlobalExceptionHandler
        authService.forgotPassword(forgotPasswordDto);
        return ResponseEntity.ok(new ApiResponse(true,"Password reset link sent to your email if the account exists."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        // InvalidTokenException, BadRequestException будут обработаны GlobalExceptionHandler
        authService.resetPassword(resetPasswordDto);
        return ResponseEntity.ok(new ApiResponse(true,"Password has been reset successfully."));
    }

    // Внутренний эндпоинт - может быть недоступен извне напрямую
    @PostMapping("/validate")
    public ResponseEntity<UserInfo> validateToken(@Valid @RequestBody ValidateTokenDto validateTokenDto) {
        try {
            UserInfo userInfo = authService.validateToken(validateTokenDto);
            return ResponseEntity.ok(userInfo);
        } catch (InvalidTokenException e) {
            // Возвращаем 401 Unauthorized для невалидного токена
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Другие ошибки обработаются GlobalExceptionHandler
    }

    // Опциональный эндпоинт
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody(required = false) LogoutDto logoutDto) { // DTO может быть необязательным
        // Если stateful, передаем DTO с refresh token для инвалидации
        authService.logout(logoutDto != null ? logoutDto : new LogoutDto()); // Передаем пустой DTO, если тело не пришло
        return ResponseEntity.ok(new ApiResponse(true,"Logout successful."));
    }


    // Вспомогательный класс для простых ответов
    @lombok.Data @lombok.AllArgsConstructor
    static class ApiResponse {
        private boolean success;
        private String message;
    }
}