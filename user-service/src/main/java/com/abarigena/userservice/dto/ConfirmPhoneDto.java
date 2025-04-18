package com.abarigena.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmPhoneDto {
    @NotBlank
    @Size(min = 4, max = 6)
    private String code;
}