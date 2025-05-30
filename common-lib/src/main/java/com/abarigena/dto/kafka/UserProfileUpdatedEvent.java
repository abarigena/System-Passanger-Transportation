package com.abarigena.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdatedEvent {
    private UUID userId;
    private Map<String, Object> updatedFields; // Поля, которые изменились { "firstName": "NewName", "photoUrl": "new.jpg" }
    private Instant timestamp;
}