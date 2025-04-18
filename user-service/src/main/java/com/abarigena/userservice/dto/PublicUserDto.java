package com.abarigena.userservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PublicUserDto {
    private UUID id;
    private String firstName;
    private String photoUrl;
    private BigDecimal averageRating;
}