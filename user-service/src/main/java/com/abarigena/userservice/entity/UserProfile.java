package com.abarigena.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_userprofile_phone", columnList = "phoneNumber", unique = true) // Если телефон должен быть уникальным
})
@Data
@NoArgsConstructor
public class UserProfile {

    @Id
    // НЕ генерируется здесь, приходит извне (от AuthService через Kafka)
    @Column(updatable = false, nullable = false)
    private UUID userId;

    // Email можно хранить для информации, получен из события регистрации
    @Column(unique = true)
    private String email;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 20, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private Boolean phoneVerified = false; // По умолчанию false

    @Column(length = 255)
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    // Точность зависит от требований
    @Column(precision = 3, scale = 2, columnDefinition = "DECIMAL(3,2) default 0.0")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @CreationTimestamp // Время создания профиля (после получения события)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}