package com.abarigena.userservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean phoneVerified;
    private String photoUrl;
    private String additionalInfo;
    private LocalDateTime registrationDate;
    private BigDecimal averageRating;
}