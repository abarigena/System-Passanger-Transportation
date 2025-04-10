package com.abarigena.authenticationservice.service;

import com.abarigena.authenticationservice.dto.*;
import com.abarigena.authenticationservice.entity.AuthUser;
import com.abarigena.authenticationservice.entity.EmailConfirmationToken;
import com.abarigena.authenticationservice.entity.PasswordResetToken;
import com.abarigena.authenticationservice.entity.UserStatus;
import com.abarigena.authenticationservice.exception.BadRequestException;
import com.abarigena.authenticationservice.exception.ConflictException;
import com.abarigena.authenticationservice.exception.InvalidTokenException;
import com.abarigena.authenticationservice.exception.ResourceNotFoundException;
import com.abarigena.authenticationservice.kafka.KafkaProducerService;
import com.abarigena.authenticationservice.repository.AuthUserRepository;
import com.abarigena.authenticationservice.repository.EmailConfirmationTokenRepository;
import com.abarigena.authenticationservice.repository.PasswordResetTokenRepository;
import com.abarigena.authenticationservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional // Весь сервис транзакционный по умолчанию
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final KafkaProducerService kafkaProducerService;

    @Value("${jwt.email-confirm-token-expiration-ms}")
    private long emailConfirmTokenExpirationMs;
    @Value("${jwt.password-reset-token-expiration-ms}")
    private long passwordResetTokenExpirationMs;

    // 1. Регистрация
    @Override
    public void register(RegisterDto registerDto) {
        if (authUserRepository.existsByEmail(registerDto.getEmail())) {
            throw new ConflictException("Error: Email is already in use!");
        }

        AuthUser user = new AuthUser();
        user.setEmail(registerDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDto.getPassword()));
        // userId генерируется автоматически JPA/Hibernate
        user.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);
        // Роли устанавливаются по умолчанию в entity

        AuthUser savedUser = authUserRepository.save(user);
        log.info("User registered successfully with email: {}", savedUser.getEmail());

        // Генерация и сохранение токена подтверждения email
        String token = UUID.randomUUID().toString();
        EmailConfirmationToken confirmationToken = new EmailConfirmationToken(
                token,
                savedUser,
                LocalDateTime.now().plus(Duration.ofMillis(emailConfirmTokenExpirationMs))
        );
        emailConfirmationTokenRepository.save(confirmationToken);
        log.info("Email confirmation token generated for user: {}", savedUser.getEmail());

        // TODO: Отправка email с ссылкой/токеном подтверждения (через Notification Service или напрямую)
        // String confirmationUrl = "http://yourapp.com/api/auth/confirm-email?token=" + token;
        log.info("Simulating sending confirmation email to {} with token {}", savedUser.getEmail(), token); // Симуляция

        // Отправка события в Kafka
        kafkaProducerService.sendUserRegisteredEvent(
                savedUser.getUserId(),
                savedUser.getEmail(),
                registerDto.getFirstName(), // Передаем опциональные поля
                registerDto.getLastName(),
                registerDto.getPhoneNumber()
        );
    }

    // 2. Подтверждение Email
    @Transactional
    @Override// Отдельная транзакция для этого метода
    public void confirmEmail(String token) {
        EmailConfirmationToken confirmationToken = emailConfirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid confirmation token"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new BadRequestException("Email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            emailConfirmationTokenRepository.delete(confirmationToken); // Удаляем истекший токен
            throw new InvalidTokenException("Confirmation token expired");
        }

        // Подтверждаем токен и активируем пользователя
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        emailConfirmationTokenRepository.save(confirmationToken); // Обновляем токен

        AuthUser user = confirmationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        authUserRepository.save(user);
        log.info("Email confirmed successfully for user: {}", user.getEmail());


        // Отправка события в Kafka
        kafkaProducerService.sendUserEmailVerifiedEvent(user.getUserId());

        // Можно удалить токен после использования, чтобы избежать повторного использования
        // emailConfirmationTokenRepository.delete(confirmationToken);
    }


    // 3. Аутентификация (Login)
    @Transactional(readOnly = true)
    @Override// Логин не изменяет данные (кроме stateful refresh)
    public TokenDto login(LoginDto loginDto) {
        // AuthenticationManager сам проверит пароль и статус (через UserDetailsServiceImpl.isEnabled)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерируем токены
        AuthUser userPrincipal = (AuthUser) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        log.info("User logged in successfully: {}", loginDto.getEmail());


        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 4. Обновление Токена (Refresh)
    @Transactional(readOnly = true)
    @Override // Read-only, если stateless refresh
    public TokenDto refreshToken(RefreshDto refreshDto) {
        String requestRefreshToken = refreshDto.getRefreshToken();

        if (requestRefreshToken == null || !jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromJWT(requestRefreshToken);

        // RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash(requestRefreshToken))
        // .orElseThrow(() -> new InvalidTokenException("Refresh token not found or revoked"));
        // if(storedToken.getExpiresAt().isBefore(LocalDateTime.now())) throw...;

        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for refresh token"));

        // Генерируем новую пару токенов
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("Tokens refreshed for user: {}", user.getEmail());


        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    // 5. Восстановление пароля - Шаг 1: Запрос на сброс
    @Override
    public void forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        AuthUser user = authUserRepository.findByEmail(forgotPasswordDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + forgotPasswordDto.getEmail()));

        // TODO: Возможно, добавить проверку, не заблокирован ли юзер

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                LocalDateTime.now().plus(Duration.ofMillis(passwordResetTokenExpirationMs))
        );
        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getEmail());

        // TODO: Отправка email с ссылкой/токеном для сброса пароля
        // String resetUrl = "http://yourfrontend.com/reset-password?token=" + token;
        log.info("Simulating sending password reset email to {} with token {}", user.getEmail(), token);
    }

    // 6. Восстановление пароля - Шаг 2: Сброс пароля новым токеном
    @Transactional
    @Override
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(resetPasswordDto.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (resetToken.getUsedAt() != null) {
            throw new BadRequestException("Password reset token already used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken); // Удаляем истекший токен
            throw new InvalidTokenException("Password reset token expired");
        }

        // Сбрасываем пароль
        AuthUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        authUserRepository.save(user);

        // Помечаем токен как использованный (или удаляем)
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        // passwordResetTokenRepository.delete(resetToken); // Альтернатива - удаление

        log.info("Password reset successfully for user: {}", user.getEmail());

        // Отправка события в Kafka
        kafkaProducerService.sendUserPasswordChangedEvent(user.getUserId());
    }


    // 7. Валидация Токена (Внутренний эндпоинт)
    @Transactional(readOnly = true)
    @Override
    public UserInfo validateToken(ValidateTokenDto validateTokenDto) {
        String token = validateTokenDto.getToken();
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            // Не используем ControllerAdvice здесь, т.к. это может быть внутренний вызов
            // Выбрасываем исключение, которое будет обработано стандартно или специально
            throw new InvalidTokenException("Invalid or expired access token");
        }
        // Валидация прошла, извлекаем информацию
        return jwtTokenProvider.getUserInfoFromJWT(token);

    }


    @Transactional
    @Override
    public void logout(LogoutDto logoutDto) {
        // Если refresh token не передан (например, просто удалили на клиенте), ничего не делаем
        if(logoutDto.getRefreshToken() == null || logoutDto.getRefreshToken().isBlank()) {
            log.info("Logout request received without refresh token.");
            return;
        }

        log.info("Logout requested. Invalidate refresh token logic needs implementation if stateful.");

    }
}
