package com.abarigena.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshDto {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}