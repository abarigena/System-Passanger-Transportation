package com.abarigena.authenticationservice.kafka;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserEmailVerifiedEventDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Instant timestamp;
}
