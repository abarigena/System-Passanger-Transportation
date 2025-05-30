package com.abarigena.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordChangedEvent {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Instant timestamp;
}
