package com.abarigena.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingUpdatedEvent {
    private UUID userId;
    private BigDecimal newAverageRating;
    private Instant timestamp;
}