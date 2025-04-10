package com.abarigena.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenDto {

    @NotBlank(message = "Token cannot be blank")
    private String token; // Access Token для валидации
}