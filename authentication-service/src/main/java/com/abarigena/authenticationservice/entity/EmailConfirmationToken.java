package com.abarigena.authenticationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_confirmation_tokens")
@Data
@NoArgsConstructor
public class EmailConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Можно использовать UUID для токена
    private UUID id; // Первичный ключ таблицы

    @Column(nullable = false, unique = true)
    private String token; // Сам токен подтверждения

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private AuthUser user;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt; // Когда токен был использован

    public EmailConfirmationToken(String token, AuthUser user, LocalDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }
}
