package com.abarigena.userservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContactVerifiedEvent {
    private UUID userId;
    private String contactType; // "phone"
    private Instant timestamp;
}